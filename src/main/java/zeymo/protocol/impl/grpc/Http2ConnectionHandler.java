package zeymo.protocol.impl.grpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import zeymo.protocol.dump.AddressAwareEmbeddedChannel;

import java.net.InetSocketAddress;
import java.util.List;

import static io.netty.buffer.ByteBufUtil.hexDump;
import static io.netty.handler.codec.http2.Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT;
import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.Http2FrameTypes.SETTINGS;
import static java.lang.Math.min;

/**
 * @author Zeymo
 */
public class Http2ConnectionHandler extends ByteToMessageDecoder {

    private ByteBuf clientPrefaceString = Http2CodecUtil.connectionPrefaceBuf();
    private static final ByteBuf HTTP_1_X_BUF = Unpooled.unreleasableBuffer(
            Unpooled.wrappedBuffer(new byte[]{'H', 'T', 'T', 'P', '/', '1', '.'})).asReadOnly();
    private Http2Connection http2Connection;
    private BaseDecoder decoder;
    private Http2FrameListener http2FrameListener;

    public Http2ConnectionHandler(Http2Connection http2Connection) {
        this.http2Connection = http2Connection;
        this.decoder = new PrefaceDecoder();
        this.http2FrameListener = new DefaultHttp2FrameListener();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decoder.decode(ctx, in, out);
    }

    protected void onHeaderFrame(ChannelHandlerContext ctx, Http2StreamFrame frame) {
        ctx.fireChannelRead(frame);
    }

    protected void onDataFrame(ChannelHandlerContext ctx, Http2StreamFrame frame) {
        ctx.fireChannelRead(frame);
    }

    protected void onRequestStreamReceive(ChannelHandlerContext ctx, StreamCreateEvent event) {
        ctx.fireUserEventTriggered(event);
    }

    public class DefaultHttp2FrameListener implements Http2FrameListener {

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
            Http2Stream http2Stream = http2Connection.stream(streamId);
            if (http2Stream == null) {
                throw new Http2Exception(Http2Error.STREAM_CLOSED);
            }
            onDataFrame(ctx, new DefaultHttp2DataFrame(data, endOfStream, padding).stream(((DefaultHttp2Stream) http2Stream).getFrameStream()));
            return 0;
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {
            onHeadersRead(ctx, streamId, headers, 0, DEFAULT_PRIORITY_WEIGHT, false, padding, endOfStream);
        }

        @Override
        public void
        onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
            Http2Stream http2Stream = http2Connection.stream(streamId);
            if (http2Stream == null) {
                AddressAwareEmbeddedChannel channel = new AddressAwareEmbeddedChannel((InetSocketAddress) ctx.channel().localAddress(),(InetSocketAddress)ctx.channel().remoteAddress());
                http2Stream = new DefaultHttp2Stream(streamId, channel);
                http2Connection.add(http2Stream);
                onRequestStreamReceive(ctx, new StreamCreateEvent(http2Stream));
            } else {

            }

            onHeaderFrame(ctx, new DefaultHttp2HeadersFrame(headers, endOfStream, padding).stream(((DefaultHttp2Stream) http2Stream).getFrameStream()));
        }

        @Override
        public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) throws Http2Exception {
            // ignore
        }

        @Override
        public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
            // ignore
        }

        @Override
        public void onSettingsAckRead(ChannelHandlerContext ctx) throws Http2Exception {
            // ignore
        }

        @Override
        public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {

        }

        @Override
        public void onPingRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
            // ignore
        }

        @Override
        public void onPingAckRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
            // ignore
        }

        @Override
        public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) throws Http2Exception {
            // ignore
        }

        @Override
        public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) throws Http2Exception {
            // ignore
        }

        @Override
        public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) throws Http2Exception {
            // ignore
        }

        @Override
        public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload) throws Http2Exception {
            // ignore
        }
    }

    class StreamCreateEvent {
        private Http2Stream http2Stream;

        public StreamCreateEvent(Http2Stream http2Stream) {
            this.http2Stream = http2Stream;
        }

        public Http2Stream getHttp2Stream() {
            return http2Stream;
        }
    }

    private boolean readClientPrefaceString(ByteBuf in) throws Http2Exception {
        if (clientPrefaceString == null) {
            return true;
        }

        int prefaceRemaining = clientPrefaceString.readableBytes();
        int bytesRead = min(in.readableBytes(), prefaceRemaining);

        // If the input so far doesn't match the preface, break the connection.
        if (bytesRead == 0 || !ByteBufUtil.equals(in, in.readerIndex(),
                clientPrefaceString, clientPrefaceString.readerIndex(),
                bytesRead)) {
            int maxSearch = 1024; // picked because 512 is too little, and 2048 too much
            int http1Index =
                    ByteBufUtil.indexOf(HTTP_1_X_BUF, in.slice(in.readerIndex(), min(in.readableBytes(), maxSearch)));
            if (http1Index != -1) {
                String chunk = in.toString(in.readerIndex(), http1Index - in.readerIndex(), CharsetUtil.US_ASCII);
                throw connectionError(PROTOCOL_ERROR, "Unexpected HTTP/1.x request: %s", chunk);
            }
            String receivedBytes = hexDump(in, in.readerIndex(),
                    min(in.readableBytes(), clientPrefaceString.readableBytes()));
            throw connectionError(PROTOCOL_ERROR, "HTTP/2 client preface string missing or corrupt. " +
                    "Hex dump for received bytes: %s", receivedBytes);
        }
        in.skipBytes(bytesRead);
        clientPrefaceString.skipBytes(bytesRead);

        if (!clientPrefaceString.isReadable()) {
            // Entire preface has been read.
            clientPrefaceString.release();
            clientPrefaceString = null;
            return true;
        }
        return false;
    }

    private boolean verifyFirstFrameIsSettings(ByteBuf in) throws Http2Exception {
        if (in.readableBytes() < 5) {
            // Need more data before we can see the frame type for the first frame.
            return false;
        }

        short frameType = in.getUnsignedByte(in.readerIndex() + 3);
        short flags = in.getUnsignedByte(in.readerIndex() + 4);
        if (frameType != SETTINGS || (flags & Http2Flags.ACK) != 0) {
            throw connectionError(PROTOCOL_ERROR, "First received frame was not SETTINGS. " +
                            "Hex dump for first 5 bytes: %s",
                    hexDump(in, in.readerIndex(), 5));
        }
        return true;
    }

    private abstract class BaseDecoder {
        public abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;
    }

    private class PrefaceDecoder extends BaseDecoder {
        @Override
        public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (ctx.channel().isActive() && readClientPrefaceString(in) && verifyFirstFrameIsSettings(in)) {
                decoder = new FrameDecoder();
                decoder.decode(ctx, in, out);
            }
        }
    }

    private class FrameDecoder extends BaseDecoder {
        private Http2FrameReader frameReader;

        public FrameDecoder() {
            this.frameReader = new DefaultHttp2FrameReader();
        }

        @Override
        public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            this.frameReader.readFrame(ctx, in, http2FrameListener);
        }
    }
}
