package zeymo.protocol.dump;

import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Date;

/**
 * @author Zeymo
 */
public class Session {
    private InetAddress srcAddress;
    private InetAddress dstAddress;
    private int srcPort;
    private int dstPort;
    private String uniqueKey;
    private Channel channel;
    public Session(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort, DumpChannelFactory channelFactory) throws Exception {
        this.srcAddress = srcAddress;
        this.srcPort = srcPort;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.uniqueKey = generateUniqueKey(srcAddress, srcPort, dstAddress, dstPort);
        this.channel = channelFactory.dumpChannel((Inet4Address) srcAddress, srcPort, (Inet4Address) dstAddress, dstPort);
    }

    public String uniqueKey() {
        return uniqueKey;
    }

    public static String generateUniqueKey(InetAddress srcAddress, int srcPort, InetAddress dstAddress, int dstPort) {
        return srcAddress.getHostAddress() + " " + srcPort + " -> " + dstAddress.getHostAddress() + " " + dstPort;
    }

    public void onMessageReceive(Packet packet, TcpPacket.TcpHeader tcpHeader, Date date) {
        channel.onBytesReceive(this, packet, tcpHeader, date);
    }
}
