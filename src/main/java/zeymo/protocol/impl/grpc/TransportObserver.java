package zeymo.protocol.impl.grpc;

import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author Zeymo
 */
public interface TransportObserver {

    /**
     * on metadata received
     *
     * @param metadata
     * @param endStream
     */
    void onMetadata(Http2Headers metadata, boolean endStream);

    /**
     * on data received
     *
     * @param data
     * @param endStream
     */
    void onData(byte[] data, boolean endStream);

    /**
     * on stream completed
     *
     */
    void onComplete(Stream.OperationHandler handler);
}
