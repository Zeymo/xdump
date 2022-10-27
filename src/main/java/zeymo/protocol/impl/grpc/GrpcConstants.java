package zeymo.protocol.impl.grpc;

/**
 * @author Zeymo
 */
public interface GrpcConstants {
    String AUTHORITY_KEY = ":authority";
    String PATH_KEY = ":path";
    String STATUS_KEY = "grpc-status";
    String MESSAGE_KEY = "grpc-message";
    String TIMEOUT = "grpc-timeout";
    String CONTENT_TYPE_KEY = "content-type";
    String CONTENT_PROTO = "application/grpc+proto";
    String APPLICATION_GRPC = "application/grpc";
}
