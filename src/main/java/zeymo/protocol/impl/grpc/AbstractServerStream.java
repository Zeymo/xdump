package zeymo.protocol.impl.grpc;

import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author Zeymo
 */
public abstract class AbstractServerStream extends AbstractStream {
    public AbstractServerStream(int streamId, QueryStringDecoder parameters, Executor executor) {
        super(streamId, parameters, executor);
    }

    @Override
    public void execute(Runnable runnable) {
        try {
            super.execute(runnable);
        } catch (RejectedExecutionException e) {
            //ignore
        } catch (Throwable t) {
            //ignore
        }
    }
}
