package zeymo.protocol.impl.grpc;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.io.PrintWriter;

/**
 * @author Zeymo
 */
public class GrpcStatus {
    public final Code code;
    public final Throwable cause;
    public final String description;
    private String scope;
    private String reason;
    private String developMessage;

    public GrpcStatus(Code code, Throwable cause, String description) {
        this.code = code;
        this.cause = cause;
        this.description = description;
    }

    public static GrpcStatus toH2Status(int status) {
        Code code;
        switch (status) {
            case 200:
                code = Code.OK;
                break;
            case 400:
                code = Code.INVALID_ARGUMENT;
                break;
            case 401:
                code = Code.UNAUTHENTICATED;
                break;
            case 404:
                code = Code.UNIMPLEMENTED;
                break;
            case 408:
                code = Code.DEADLINE_EXCEEDED;
                break;
            case 416:
                code = Code.OUT_OF_RANGE;
                break;
            case 420:
                code = Code.FAILED_PRECONDITION;
                break;
            case 500:
                code = Code.INTERNAL;
                break;
            case 503:
                code = Code.UNAVAILABLE;
                break;
            case 600:
                code = Code.RESOURCE_EXHAUSTED;
                break;
            default:
                code = Code.UNKNOWN;
                break;
        }
        return fromCode(code);
    }

    public static GrpcStatus fromCode(int code) {
        return fromCode(Code.fromCode(code));
    }

    public static GrpcStatus fromCode(Code code) {
        return new GrpcStatus(code, null, null);
    }

    public static int toLwpStatus(Code code) {
        int status;
        switch (code) {
            case OK:
                status = HttpResponseStatus.OK.code();
                break;
            case UNKNOWN:
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
                break;
            case DEADLINE_EXCEEDED:
                status = HttpResponseStatus.REQUEST_TIMEOUT.code();
                break;
            case RESOURCE_EXHAUSTED:
                status = HttpResponseStatus.SERVICE_UNAVAILABLE.code();
                break;
            case UNIMPLEMENTED:
                status = HttpResponseStatus.NOT_FOUND.code();
                break;
            case INVALID_ARGUMENT:
                status = HttpResponseStatus.BAD_REQUEST.code();
                break;
            case INTERNAL:
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
                break;
            case UNAVAILABLE:
            case DATA_LOSS:
                status = HttpResponseStatus.SERVICE_UNAVAILABLE.code();
                break;
            case UNAUTHENTICATED:
                status = HttpResponseStatus.UNAUTHORIZED.code();
                break;
            default:
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
                break;
        }
        return status;
    }

    public static String limitSizeTo4KB(String desc) {
        if (desc.length() < 4096) {
            return desc;
        } else {
            return desc.substring(0, 4086);
        }
    }

    public static String fromMessage(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        return QueryStringDecoder.decodeComponent(raw);
    }

    public GrpcStatus withCause(Throwable cause) {
        return new GrpcStatus(this.code, cause, this.description);
    }

    public GrpcStatus withDescription(String description) {
        return new GrpcStatus(this.code, this.cause, description);
    }

    public String toMessage() {
        final String msg;
        if (cause == null) {
            msg = description;
        } else {
            String placeHolder = description == null ? "" : description;
            msg = toString(placeHolder, cause);
        }
        if (msg == null) {
            return "";
        }
        String output = limitSizeTo4KB(msg);
        QueryStringEncoder encoder = new QueryStringEncoder("");
        encoder.addParam("", output);
        // ?=
        return encoder.toString().substring(2);
    }

    public static String toString(String msg, Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);

        String str;
        try {
            e.printStackTrace(p);
            str = w.toString();
        } finally {
            p.close();
        }

        return str;
    }

    public enum Code {
        OK(0),
        CANCELLED(1),
        UNKNOWN(2),
        INVALID_ARGUMENT(3),
        DEADLINE_EXCEEDED(4),
        NOT_FOUND(5),
        ALREADY_EXISTS(6),
        PERMISSION_DENIED(7),
        RESOURCE_EXHAUSTED(8),
        FAILED_PRECONDITION(9),
        ABORTED(10),
        OUT_OF_RANGE(11),
        UNIMPLEMENTED(12),
        INTERNAL(13),
        UNAVAILABLE(14),
        DATA_LOSS(15),
        UNAUTHENTICATED(16);

        final int code;

        Code(int code) {
            this.code = code;
        }

        public static boolean isOk(Integer status) {
            return status == OK.code;
        }

        public static Code fromCode(int code) {
            for (Code value : Code.values()) {
                if (value.code == code) {
                    return value;
                }
            }
            throw new IllegalStateException("Can not find status for code: " + code);
        }
    }

}

