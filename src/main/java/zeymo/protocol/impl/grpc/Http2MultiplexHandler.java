package zeymo.protocol.impl.grpc;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;

/**
 * @author Zeymo
 */
public class Http2MultiplexHandler extends ChannelDuplexHandler {

    public final static String STREAM_HANDLER_KEY = "streamHandler";
    private ChannelHandler inboundChannelHandler;

    public Http2MultiplexHandler(ChannelHandler inboundChannelHandler) {
        this.inboundChannelHandler = inboundChannelHandler;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof Http2ConnectionHandler.StreamCreateEvent) {
            DefaultHttp2Stream http2Stream = (DefaultHttp2Stream) ((Http2ConnectionHandler.StreamCreateEvent) event).getHttp2Stream();
            if (inboundChannelHandler != null && http2Stream.channel().pipeline().get(STREAM_HANDLER_KEY) == null) {
                http2Stream.channel().pipeline().addLast(STREAM_HANDLER_KEY, inboundChannelHandler);
            }
        }
        ctx.fireUserEventTriggered(event);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2StreamFrame) {
            Http2StreamFrame http2StreamFrame = (Http2StreamFrame) msg;
            EmbeddedChannel streamChannel = ((DefaultHttp2FrameStream) http2StreamFrame.stream()).channel();
            if (msg instanceof Http2DataFrame) {
                ((Http2DataFrame) msg).content().retain();
            }
            streamChannel.writeInbound(msg);
            streamChannel.readInbound();
            return;
        }
        ctx.fireChannelRead(msg);
    }
}
