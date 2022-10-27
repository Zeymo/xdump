package zeymo.protocol.dump;

import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.util.Date;

/**
 * @author Zeymo
 */
public interface Channel {

    /**
     * on bytes received
     *
     * @param session
     * @param packet
     * @param tcpHeader
     * @param date
     */
    void onBytesReceive(Session session, Packet packet, TcpPacket.TcpHeader tcpHeader, Date date);
}
