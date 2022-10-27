package zeymo.protocol.dump;

import java.util.List;
import java.util.Map;

/**
 * @author Zeymo
 */
public class Response extends Message<Integer>{
    public Response(Integer startLine, Map<String, List<String>> headers, Content body) {
        super(startLine, headers, body);
    }
}
