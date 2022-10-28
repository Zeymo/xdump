package zeymo.protocol.dump;

import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Zeymo
 */
public class Session {

    private Channel channel;
    private ScheduledExecutorService executor;

    public Session(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort, DumpChannelFactory channelFactory) throws Exception {
        this.channel = channelFactory.dumpChannel((Inet4Address) srcAddress, srcPort, (Inet4Address) dstAddress, dstPort);
        this.executor = EventLoopRuntime.getInstance().getEventLoop();
    }

    public static String generateUniqueKey(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort) {
        return srcAddress.getHostAddress() + " " + srcPort + " -> " + dstAddress.getHostAddress() + " " + dstPort;
    }

    public void onMessageReceive(Packet packet, TcpPacket.TcpHeader tcpHeader, Date date) {
        executor.submit(() -> channel.onBytesReceive(this, packet, tcpHeader, date));
    }
}
