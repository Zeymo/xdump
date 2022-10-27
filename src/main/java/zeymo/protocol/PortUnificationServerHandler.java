package zeymo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

public class PortUnificationServerHandler extends ByteToMessageDecoder {
    private final URI uri;
    private final List<Protocol> protocols;

    public PortUnificationServerHandler(URI uri, List<Protocol> protocols) throws Exception {
        this.uri = uri;
        this.protocols = protocols;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() >= 5) {

            Iterator iterator = this.protocols.iterator();

            while (iterator.hasNext()) {
                Protocol protocol = (Protocol) iterator.next();
                in.markReaderIndex();
                ProtocolDetector.Result result = protocol.detector().detect(ctx, in);
                in.resetReaderIndex();
                switch (result) {
                    case UNRECOGNIZED:
                    default:
                        break;
                    case RECOGNIZED:
                        protocol.configPipeline(uri,ctx.pipeline());
                        ctx.pipeline().remove(this);
                    case NEED_MORE_DATA:
                        return;
                }
            }
            in.clear();
            ctx.close();
        }
    }
}
