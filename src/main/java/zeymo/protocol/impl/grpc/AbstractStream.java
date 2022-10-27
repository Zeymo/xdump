package zeymo.protocol.impl.grpc;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http2.Http2Headers;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Zeymo
 */
public abstract class AbstractStream implements Stream {
    public static final String PEER_KEY = "peer";
    private final int streamId;
    private final QueryStringDecoder url;
    private final Executor executor;
    private final TransportObserver transportObserver;

    public AbstractStream(int streamId, QueryStringDecoder url, Executor executor) {
        this.streamId = streamId;
        this.url = url;
        this.executor = executor;
        this.transportObserver = createTransportObserver();
    }

    public int getStreamId() {
        return streamId;
    }

    public QueryStringDecoder url() {
        return url;
    }

    @Override
    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    protected abstract TransportObserver createTransportObserver();

    @Override
    public TransportObserver asTransportObserver() {
        return transportObserver;
    }

    protected static abstract class AbstractTransportObserver implements TransportObserver {
        private Http2Headers headers;
        private Http2Headers trailers;

        public Http2Headers getHeaders() {
            return headers;
        }

        public Http2Headers getTrailers() {
            return trailers;
        }

        @Override
        public void onMetadata(Http2Headers metadata, boolean endStream) {
            if (headers == null) {
                headers = metadata;
            } else {
                trailers = metadata;
            }
        }

        protected Headers.Builder parseHttp2HeadersToHeaders(Http2Headers http2Headers) {
            Headers.Builder builder = new Headers.Builder();
            for (Map.Entry<CharSequence, CharSequence> header : http2Headers) {
                String key = header.getKey().toString();
                if (Http2Headers.PseudoHeaderName.isPseudoHeader(key)) {
                    continue;
                }
                if (key.startsWith("trailer")) {
                    // server compatible HTTP/1.1 like client (e.g. golang net/http) will declare 'Trailer' for status and message
                    // standard HTTP/2 or gRPC protocol can ignore it
                    continue;
                }
                builder.add(key, header.getValue().toString());
            }
            return builder;
        }

        protected GrpcStatus extractStatusFromMeta(Http2Headers metadata) {
            if (metadata.contains(GrpcConstants.STATUS_KEY)) {
                final int code = Integer.parseInt(metadata.get(GrpcConstants.STATUS_KEY).toString());

                if (!GrpcStatus.Code.isOk(code)) {
                    GrpcStatus status = GrpcStatus.fromCode(code);
                    if (metadata.contains(GrpcConstants.MESSAGE_KEY)) {
                        final String raw = metadata.get(GrpcConstants.MESSAGE_KEY).toString();
                        status = status.withDescription(GrpcStatus.fromMessage(raw));
                    }
                    return status;
                }
                return GrpcStatus.fromCode(GrpcStatus.Code.OK);
            }
            return GrpcStatus.fromCode(GrpcStatus.Code.OK);
        }
    }

    protected abstract static class UnaryTransportObserver extends AbstractTransportObserver implements TransportObserver {
        private byte[] data;

        public byte[] getData() {
            return data;
        }

        protected abstract void onError(GrpcStatus status);

        @Override
        public void onComplete(OperationHandler handler) {
            Http2Headers metadata;
            if (getTrailers() == null) {
                metadata = getHeaders();
            } else {
                metadata = getTrailers();
            }

            final GrpcStatus status = extractStatusFromMeta(metadata);
            if (GrpcStatus.Code.isOk(status.code.code)) {
                doOnComplete(handler);
            } else {
                onError(status);
            }
        }

        protected abstract void doOnComplete(OperationHandler handler);

        @Override
        public void onData(byte[] in, boolean endStream) {
            if (data == null) {
                this.data = in;
            }
        }
    }
}
