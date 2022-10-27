package zeymo.protocol.impl.grpc;

/**
 * @author
 */
public interface Stream {

    TransportObserver asTransportObserver();

    void execute(Runnable runnable);

    interface OperationHandler {

        /**
         * @param result operation's result
         * @param cause  null if the operation succeed
         */
        void operationDone(OperationResult result, Throwable cause);
    }

    enum OperationResult {
        OK,
        FAILURE,
        NETWORK_FAIL
    }
}
