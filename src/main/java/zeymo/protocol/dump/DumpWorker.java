package zeymo.protocol.dump;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.EOFException;
import java.net.Inet4Address;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author Zeymo
 */
public class DumpWorker {
    private PcapNetworkInterface networkInterface;
    private EventExecutorGroup eventLoop = new DefaultEventExecutorGroup(1);
    private ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();
    private DumpChannelFactory dumpChannelFactory;
    private Configuration configuration;

    public DumpWorker(PcapNetworkInterface networkInterface, Configuration configuration) {
        this.networkInterface = networkInterface;
        this.configuration = configuration;
        this.dumpChannelFactory = new DumpChannelFactory(configuration);
    }

    public void start() throws PcapNativeException, NotOpenException {
        PcapHandle handle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        handle.setFilter(configuration.getFilter(), BpfProgram.BpfCompileMode.OPTIMIZE);

        eventLoop.submit(() -> {
            while (true) {
                try {
                    Packet packet = handle.getNextPacketEx();
                    TcpPacket tcp = packet.get(TcpPacket.class);
                    IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);

                    if (tcp == null) {
                        continue;
                    }

                    TcpPacket.TcpHeader tcpHeader = tcp.getHeader();

                    boolean syn = tcpHeader.getSyn();
                    boolean fin = tcpHeader.getFin();
                    int srcPort = tcpHeader.getSrcPort().valueAsInt();
                    int dstPort = tcpHeader.getDstPort().valueAsInt();

                    IpV4Packet.IpV4Header ipV4Header = ipV4Packet.getHeader();
                    Inet4Address srcAddress = ipV4Header.getSrcAddr();
                    Inet4Address dstAddress = ipV4Header.getDstAddr();

                    String uniqueKey = Session.generateUniqueKey(srcAddress, srcPort, dstAddress, dstPort);
                    if (syn) {
                        AnsiLog.info("new tcp connection: " + uniqueKey);
                    } else if (fin) {
                        sessions.remove(uniqueKey);
                        AnsiLog.info("tcp connection close: " + uniqueKey);
                    } else {
                        Session session = sessions.get(uniqueKey);
                        if (session == null) {
                            String peerUniqueKey = Session.generateUniqueKey(dstAddress, dstPort, srcAddress, srcPort);
                            session = sessions.get(peerUniqueKey);
                            if (session == null) {
                                session = new Session(srcAddress, srcPort, dstAddress, dstPort, dumpChannelFactory);
                                sessions.put(uniqueKey, session);
                            }
                        }

                        Packet payload = tcp.getPayload();
                        if (payload != null) {
                            session.onMessageReceive(payload, tcpHeader, new Date());
                        }
                    }
                } catch (TimeoutException e) {
                    continue;
                } catch (EOFException e) {
                    break;
                } catch (PcapNativeException e) {
                    e.printStackTrace();
                } catch (NotOpenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
