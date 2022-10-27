package zeymo.protocol.impl.grpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2CodecUtil;
import zeymo.protocol.ProtocolDetector;

/**
 * @author Zeymo
 */
public class Http2ProtocolDetector implements ProtocolDetector {
    private final ByteBuf clientPrefaceString = Http2CodecUtil.connectionPrefaceBuf();

    @Override
    public Result detect(ChannelHandlerContext ctx, ByteBuf in) {
        int prefaceLen = this.clientPrefaceString.readableBytes();
        int bytesRead = Math.min(in.readableBytes(), prefaceLen);
        if (bytesRead != 0 && ByteBufUtil.equals(in, 0, this.clientPrefaceString, 0, bytesRead)) {
            return bytesRead == prefaceLen ? Result.RECOGNIZED : Result.NEED_MORE_DATA;
        } else {
            return Result.UNRECOGNIZED;
        }
    }
}
