package zeymo.protocol;

import io.netty.channel.ChannelPipeline;

import java.net.URI;

/**
 * @author Zeymo
 */
public interface Protocol {
    /**
     * detector
     *
     * @return
     */
    ProtocolDetector detector();

    /**
     * config pipeline
     *
     * @param pipeline
     * @throws Exception
     */
    void configPipeline(URI uri, ChannelPipeline pipeline) throws Exception;

    /**
     * name of protocol
     *
     * @return
     */
    String name();
}
