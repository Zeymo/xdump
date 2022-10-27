package zeymo.protocol.impl.grpc;


import io.netty.handler.codec.http.QueryStringDecoder;
import zeymo.protocol.dump.AnsiPrinter;
import zeymo.protocol.dump.Content;
import zeymo.protocol.dump.Request;

import java.util.concurrent.Executor;

/**
 * @author Zeymo
 */
public class GrpcRequestStream extends AbstractServerStream {

    public GrpcRequestStream(int streamId, QueryStringDecoder parameters, Executor executor) {
        super(streamId, parameters, executor);
    }

    @Override
    protected TransportObserver createTransportObserver() {
        return new RequestStreamObserver();
    }

    private class RequestStreamObserver extends UnaryTransportObserver implements TransportObserver {
        @Override
        protected void onError(GrpcStatus status) {
            //ignore
        }

        @Override
        public void doOnComplete(OperationHandler handler) {
            if (getData() == null) {
                onError(GrpcStatus.fromCode(GrpcStatus.Code.INTERNAL)
                        .withDescription("Missing request data"));
                return;
            }
            execute(this::invoke);
            handler.operationDone(null, null);
        }

        public void invoke() {
            Headers headers = parseHttp2HeadersToHeaders(getHeaders()).build();
            Request request = new Request(getHeaders().path().toString(), headers.toMultimap(), Content.content(getData()));
            AnsiPrinter.print(String.valueOf(getStreamId()), GrpcProtocol.INSTANCE.name(), request, url().parameters().get(PEER_KEY).get(0));
        }
    }
}
