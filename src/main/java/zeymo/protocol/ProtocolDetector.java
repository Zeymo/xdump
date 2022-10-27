package zeymo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Zeymo
 */
public interface ProtocolDetector {
    /**
     * detect protocol
     *
     * @param ctx
     * @param in
     * @return
     */
    Result detect(final ChannelHandlerContext ctx, final ByteBuf in);

    enum Result {
        RECOGNIZED,
        UNRECOGNIZED,
        NEED_MORE_DATA;

        private Result() {
        }
    }
}
