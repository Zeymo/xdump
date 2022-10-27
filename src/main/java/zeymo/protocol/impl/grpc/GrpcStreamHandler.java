package zeymo.protocol.impl.grpc;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2Frame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import zeymo.protocol.dump.AnsiPrinter;

import java.net.URI;
import java.net.URISyntaxException;

import static zeymo.protocol.impl.grpc.AbstractStream.PEER_KEY;

/**
 * @author Zeymo
 */
public class GrpcStreamHandler extends ChannelDuplexHandler {
    public static final AttributeKey<AbstractStream> REQUEST_STREAM_KEY = AttributeKey.newInstance("request_stream");

    private URI uri;

    private Http2Connection http2Connection;

    public GrpcStreamHandler(URI uri, Http2Connection http2Connection) {
        this.uri = uri;
        this.http2Connection = http2Connection;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            this.onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            this.onDataRead(ctx, (Http2DataFrame) msg);
        } else if (msg instanceof Http2Frame) {
            ReferenceCountUtil.release(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    public void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame msg) throws Exception {
        final Http2Headers headers = msg.headers();
        int streamId = msg.stream().id();
        if (headers.path() != null) {
            final AbstractServerStream stream = asRequestStream(ctx, streamId);
            final TransportObserver observer = stream.asTransportObserver();
            observer.onMetadata(headers, false);
            if (msg.isEndStream()) {
                observer.onComplete((result, cause) -> {
                    http2Connection.close(streamId);
                });
            }
            ctx.channel().attr(REQUEST_STREAM_KEY).set(stream);
        } else {
            DefaultHttp2Stream http2Stream = (DefaultHttp2Stream) http2Connection.stream(streamId);
            AbstractStream stream = http2Stream.asResponseStream();
            if (stream == null) {
                stream = http2Stream.createResponseStream(ctx, uri);
            }

            final TransportObserver observer = stream.asTransportObserver();
            observer.onMetadata(headers, false);
            if (msg.isEndStream()) {
                observer.onComplete((result, cause) -> {
                    http2Connection.close(msg.stream().id());
                });
            }
        }
    }

    public void onDataRead(ChannelHandlerContext ctx, Http2DataFrame msg) throws Exception {
        super.channelRead(ctx, msg.content());
        if (msg.isEndStream()) {
            final AbstractStream stream = ctx.channel().attr(GrpcStreamHandler.REQUEST_STREAM_KEY).get();
            if (stream != null) {
                stream.asTransportObserver().onComplete((result, cause) -> {
                });
            }
        }
    }

    public AbstractServerStream asRequestStream(ChannelHandlerContext ctx, int streamId) throws URISyntaxException {
        QueryStringEncoder encoder = new QueryStringEncoder(uri.toString());
        encoder.addParam(PEER_KEY, AnsiPrinter.channelString(ctx.channel().localAddress(), ctx.channel().remoteAddress()));
        QueryStringDecoder url = new QueryStringDecoder(encoder.toUri());
        return new GrpcRequestStream(streamId, url, ctx.executor());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //ignore
    }
}
