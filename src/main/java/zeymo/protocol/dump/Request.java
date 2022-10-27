package zeymo.protocol.dump;

import java.util.List;
import java.util.Map;

/**
 * @author Zeymo
 */
public class Request extends Message<String> {
    public Request(String startLine, Map<String, List<String>> headers, Content body) {
        super(startLine, headers, body);
    }

}