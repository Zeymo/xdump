package zeymo.protocol.dump;

import java.net.Inet4Address;

/**
 * @author Zeymo
 */
public class DumpChannelFactory {

    private Configuration configuration;

    public DumpChannelFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public Channel dumpChannel(Inet4Address srcAddress, int srcPort, Inet4Address dstAddress, int dstPort) throws Exception {
        return new DumpChannel(configuration, srcAddress, srcPort, dstAddress, dstPort);
    }
}
