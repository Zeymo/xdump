package zeymo.protocol.dump;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Zeymo
 */
public abstract class Message<S> {
    private final S startLine;
    private final Map<String, List<String>> headers;
    private final Content body;


    protected Message(S startLine, Map<String, List<String>> headers, Content body) {
        this.startLine = startLine;
        this.headers = headers;
        this.body = body;
    }

    public S startLine() {
        return startLine;
    }

    public Content body() {
        return body;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public String peek(String header) {
        List<String> list = get(header);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> get(String header) {
        final List<String> cur = headers.get(header);
        if (cur == null) {
            return Collections.emptyList();
        }
        return cur;
    }
}
