package zeymo.protocol.dump;

import io.netty.channel.embedded.EmbeddedChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Zeymo
 */
public class AddressAwareEmbeddedChannel extends EmbeddedChannel {
    private InetSocketAddress srcAddress;
    private InetSocketAddress dstAddress;

    public AddressAwareEmbeddedChannel(InetSocketAddress srcAddress, InetSocketAddress dstAddress) {
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
    }

    @Override
    public SocketAddress localAddress0() {
        return srcAddress;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return dstAddress;
    }
}
