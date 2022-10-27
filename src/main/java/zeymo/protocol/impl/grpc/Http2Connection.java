package zeymo.protocol.impl.grpc;

import io.netty.handler.codec.http2.Http2Stream;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

/**
 * @author Zeymo
 */
public class Http2Connection {
    private final IntObjectMap<Http2Stream> streamMap = new IntObjectHashMap<>();

    public Http2Stream stream(int streamId) {
        return streamMap.get(streamId);
    }

    public void add(Http2Stream http2Stream) {
        streamMap.put(http2Stream.id(), http2Stream);
    }

    public void close(int streamId) {
        streamMap.remove(streamId);
    }
}
