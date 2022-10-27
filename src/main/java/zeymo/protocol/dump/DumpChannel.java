package zeymo.protocol.dump;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import zeymo.protocol.PortUnificationServerHandler;
import zeymo.protocol.impl.grpc.GrpcProtocol;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Date;

/**
 * @author Zeymo
 */
public class DumpChannel implements Channel {
    private Configuration configuration;
    private EmbeddedChannel channel;


    public DumpChannel(Configuration configuration, Inet4Address srcAddress, int srcPort, Inet4Address dstAddress, int dstPort) throws Exception {
        this.configuration = configuration;
        channel = new AddressAwareEmbeddedChannel(new InetSocketAddress(srcAddress, srcPort), new InetSocketAddress(dstAddress, dstPort));
        channel.pipeline().addLast(new PortUnificationServerHandler(URI.create("gRPC://embedded"), Lists.newArrayList(GrpcProtocol.INSTANCE)));
    }

    @Override
    public void onBytesReceive(Session session, Packet packet, TcpPacket.TcpHeader tcpHeader, Date date) {
        try {
            byte[] bytes = packet.getRawData();
            ByteBuf buf = Unpooled.wrappedBuffer(bytes);
            channel.writeInbound(buf);
            channel.readInbound();
        } catch (Throwable t) {
           t.printStackTrace();
        }
    }
}
