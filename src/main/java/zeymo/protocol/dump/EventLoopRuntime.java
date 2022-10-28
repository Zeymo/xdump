package zeymo.protocol.dump;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @author Zeymo
 */
public class EventLoopRuntime {
    private final EventLoopGroup eventLoopGroup;

    private static class Holder {
        private static final EventLoopRuntime INSTANCE = new EventLoopRuntime();
    }

    public static EventLoopRuntime getInstance() {
        return Holder.INSTANCE;
    }

    private EventLoopRuntime() {
        this.eventLoopGroup =
                new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new DefaultThreadFactory("dump-event-loop"));
    }

    public EventLoop getEventLoop() {
        return this.eventLoopGroup.next();
    }
}
