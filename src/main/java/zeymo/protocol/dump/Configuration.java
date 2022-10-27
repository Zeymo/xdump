package zeymo.protocol.dump;

/**
 * @author Zeymo
 */
public class Configuration {

    private String protocol;
    private boolean verbose = false;
    private boolean ignoreHeartbeat = false;
    private String filter;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isIgnoreHeartbeat() {
        return ignoreHeartbeat;
    }

    public void setIgnoreHeartbeat(boolean ignoreHeartbeat) {
        this.ignoreHeartbeat = ignoreHeartbeat;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "protocol='" + protocol + '\'' +
                ", verbose=" + verbose +
                ", ignoreHeartbeat=" + ignoreHeartbeat +
                ", filter='" + filter + '\'' +
                '}';
    }
}
