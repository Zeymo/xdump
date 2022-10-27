package zeymo.protocol.impl.grpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import zeymo.protocol.dump.Content;

import java.util.List;

/**
 * @author Zeymo
 */
public class GrpcDataDecoder extends ReplayingDecoder<GrpcDataDecoder.GrpcDecodeState> {
    private static final int RESERVED_MASK = 0xFE;
    private static final int COMPRESSED_FLAG_MASK = 1;
    private final int maxDataSize;

    private int len;
    private boolean compressedFlag;

    public GrpcDataDecoder(int maxDataSize) {
        super(GrpcDecodeState.HEADER);
        this.maxDataSize = maxDataSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER:
                int type = in.readByte();
                if ((type & RESERVED_MASK) != 0) {

                }

                compressedFlag = (type & COMPRESSED_FLAG_MASK) != 0;

                len = in.readInt();
                if (len < 0 || len > maxDataSize) {
                    // FIXME
                }
                checkpoint(GrpcDecodeState.PAYLOAD);
            case PAYLOAD:
                byte[] dst = new byte[len];
                in.readBytes(dst);
                Content content;
                if (compressedFlag) {
                    content = new Content.DelayUnzipContent(dst);
                } else {
                    content = Content.content(dst);
                }
                out.add(content);
                checkpoint(GrpcDecodeState.HEADER);
                break;
            default:
                throw new RuntimeException("Should not reach here");
        }
    }

    enum GrpcDecodeState {
        HEADER,
        PAYLOAD
    }
}
