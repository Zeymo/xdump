package zeymo.protocol.impl.grpc;


import io.netty.handler.codec.http.QueryStringDecoder;
import zeymo.protocol.dump.AnsiPrinter;
import zeymo.protocol.dump.Content;
import zeymo.protocol.dump.Response;

import java.util.concurrent.Executor;


/**
 * @author Zeymo
 */
public class GrpcResponseStream extends AbstractStream {

    public GrpcResponseStream(int streamId, QueryStringDecoder parameters, Executor executor) {
        super(streamId, parameters, executor);
    }

    @Override
    protected TransportObserver createTransportObserver() {
        return new ResponseStreamObserver();
    }

    private class ResponseStreamObserver extends UnaryTransportObserver implements TransportObserver {

        @Override
        protected void onError(GrpcStatus grpcStatus) {
            int status = GrpcStatus.toLwpStatus(grpcStatus.code);
            Headers headers = parseHttp2HeadersToHeaders(getHeaders()).build();
            Response response = new Response(status, headers.toMultimap(), Content.content(getData()));
            AnsiPrinter.print(String.valueOf(getStreamId()), GrpcProtocol.INSTANCE.name(), response, url().parameters().get(PEER_KEY).get(0));
        }

        @Override
        protected void doOnComplete(OperationHandler handler) {
            execute(this::invoke);
            handler.operationDone(null, null);
        }

        private void invoke() {
            try {
                Headers headers = parseHttp2HeadersToHeaders(getHeaders()).build();
                GrpcStatus grpcStatus = extractStatusFromMeta(getTrailers() == null ? getHeaders() : getTrailers());
                int status = GrpcStatus.toLwpStatus(grpcStatus.code);
                Content content = Content.NO_CONTENT;
                if (getData() != null) {
                    content = Content.content(getData());
                }
                Response response = new Response(status, headers.toMultimap(), content);
                AnsiPrinter.print(String.valueOf(getStreamId()), GrpcProtocol.INSTANCE.name(), response, url().parameters().get(PEER_KEY).get(0));
            } catch (Throwable e) {
                final GrpcStatus status = GrpcStatus.fromCode(GrpcStatus.Code.INTERNAL)
                        .withCause(e)
                        .withDescription("Failed to deserialize response");
                onError(status);
            }
        }
    }
}
