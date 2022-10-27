package zeymo.protocol.impl.grpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Stream;
import zeymo.protocol.dump.AnsiPrinter;

import java.net.URI;
import java.net.URISyntaxException;

import static zeymo.protocol.impl.grpc.AbstractStream.PEER_KEY;

/**
 * @author Zeymo
 */
public class DefaultHttp2Stream implements Http2Stream {

    private int id;
    private EmbeddedChannel channel;
    private DefaultHttp2FrameStream frameStream;
    private AbstractStream responseStream;

    public DefaultHttp2Stream(int id, EmbeddedChannel channel) {
        this.id = id;
        this.channel = channel;
        this.frameStream = new DefaultHttp2FrameStream(id, channel);
    }

    public EmbeddedChannel channel() {
        return channel;
    }

    public DefaultHttp2FrameStream getFrameStream() {
        return frameStream;
    }

    public AbstractStream createResponseStream(ChannelHandlerContext ctx, URI uri) throws URISyntaxException {
        QueryStringEncoder encoder = new QueryStringEncoder(uri.toString());
        encoder.addParam(PEER_KEY, AnsiPrinter.channelString(ctx.channel().remoteAddress(), ctx.channel().localAddress()));
        QueryStringDecoder url = new QueryStringDecoder(encoder.toUri());
        GrpcResponseStream responseStream = new GrpcResponseStream(id, url, ctx.executor());
        this.responseStream = responseStream;
        return this.responseStream;
    }

    public AbstractStream asResponseStream() {
        return responseStream;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public State state() {
        return null;
    }

    @Override
    public Http2Stream open(boolean halfClosed) throws Http2Exception {
        return null;
    }

    @Override
    public Http2Stream close() {
        return null;
    }

    @Override
    public Http2Stream closeLocalSide() {
        return null;
    }

    @Override
    public Http2Stream closeRemoteSide() {
        return null;
    }

    @Override
    public boolean isResetSent() {
        return false;
    }

    @Override
    public Http2Stream resetSent() {
        return null;
    }

    @Override
    public <V> V setProperty(Http2Connection.PropertyKey key, V value) {
        return null;
    }

    @Override
    public <V> V getProperty(Http2Connection.PropertyKey key) {
        return null;
    }

    @Override
    public <V> V removeProperty(Http2Connection.PropertyKey key) {
        return null;
    }

    @Override
    public Http2Stream headersSent(boolean isInformational) {
        return null;
    }

    @Override
    public boolean isHeadersSent() {
        return false;
    }

    @Override
    public boolean isTrailersSent() {
        return false;
    }

    @Override
    public Http2Stream headersReceived(boolean isInformational) {
        return null;
    }

    @Override
    public boolean isHeadersReceived() {
        return false;
    }

    @Override
    public boolean isTrailersReceived() {
        return false;
    }

    @Override
    public Http2Stream pushPromiseSent() {
        return null;
    }

    @Override
    public boolean isPushPromiseSent() {
        return false;
    }
}
