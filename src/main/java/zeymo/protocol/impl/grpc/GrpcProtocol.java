package zeymo.protocol.impl.grpc;

import io.netty.channel.*;
import zeymo.protocol.Protocol;
import zeymo.protocol.dump.Content;

import java.net.URI;

/**
 * @author Zeymo
 */
public class GrpcProtocol extends Http2WireProtocol {
    public static final Protocol INSTANCE = new GrpcProtocol();

    private GrpcProtocol() {
    }

    @Override
    public void configPipeline(URI uri, ChannelPipeline pipeline) {
        Http2Connection http2Connection = new Http2Connection();
        Http2ConnectionHandler codec = new Http2ConnectionHandler(http2Connection);
        Http2MultiplexHandler handler = new Http2MultiplexHandler(new H2DumpInitializer(uri, http2Connection));
        pipeline.addLast(codec, handler, new SimpleChannelInboundHandler<Object>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                // just release
            }
        });
    }

    @Override
    public String name() {
        return "gRPC";
    }

    public class H2DumpInitializer extends ChannelInitializer<Channel> {
        private URI uri;
        private Http2Connection http2Connection;

        public H2DumpInitializer(URI uri, Http2Connection http2Connection) {
            this.uri = uri;
            this.http2Connection = http2Connection;
        }

        @Override
        protected void initChannel(Channel channel) {
            final ChannelPipeline p = channel.pipeline();
            p.addLast(new GrpcStreamHandler(uri, http2Connection));
            p.addLast(new GrpcDataDecoder(Integer.MAX_VALUE));
            p.addLast(new Http2ResponseStreamInboundHandler(http2Connection));
            p.addLast(new Http2RequestStreamInboundHandler());

        }
    }

    public class Http2ResponseStreamInboundHandler extends ChannelInboundHandlerAdapter {
        private final Http2Connection http2Connection;

        public Http2ResponseStreamInboundHandler(Http2Connection http2Connection) {
            this.http2Connection = http2Connection;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            AbstractStream stream = ctx.channel().attr(GrpcStreamHandler.REQUEST_STREAM_KEY).get();
            DefaultHttp2Stream http2Stream = (DefaultHttp2Stream) http2Connection.stream(stream.getStreamId());
            if (http2Stream != null && http2Stream.asResponseStream() != null) {
                byte[] data = ((Content) msg).bytes();
                http2Stream.asResponseStream().asTransportObserver().onData(data, false);
                return;
            }
            super.channelRead(ctx, msg);
        }
    }

    public class Http2RequestStreamInboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            AbstractStream stream = ctx.channel().attr(GrpcStreamHandler.REQUEST_STREAM_KEY).get();
            if (stream != null) {
                byte[] data = ((Content) msg).bytes();
                stream.asTransportObserver().onData(data, false);
            }
        }
    }
}
