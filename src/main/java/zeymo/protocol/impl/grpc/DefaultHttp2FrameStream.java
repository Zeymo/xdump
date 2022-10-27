package zeymo.protocol.impl.grpc;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2Stream;

/**
 * @author Zeymo
 */
public class DefaultHttp2FrameStream implements Http2FrameStream {
    private int id;
    private EmbeddedChannel channel;

    public DefaultHttp2FrameStream(int id, EmbeddedChannel channel) {
        this.id = id;
        this.channel = channel;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public Http2Stream.State state() {
        return null;
    }

    public EmbeddedChannel channel() {
        return channel;
    }
}
