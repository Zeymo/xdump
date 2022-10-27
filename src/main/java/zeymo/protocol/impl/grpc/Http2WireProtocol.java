package zeymo.protocol.impl.grpc;

import zeymo.protocol.Protocol;
import zeymo.protocol.ProtocolDetector;

/**
 * @author Zeymo
 */
public abstract class Http2WireProtocol implements Protocol {
    private final ProtocolDetector detector = new Http2ProtocolDetector();

    @Override
    public ProtocolDetector detector() {
        return this.detector;
    }
}
