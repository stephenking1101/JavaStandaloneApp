package example.foundation.servicediscovery.support;

public class Node {
    private String nodename;
    private String hostname;
    private String internalIp;
    private String externalIp;

    public Node() {
    }

    public String getNodename() {
        return this.nodename;
    }

    public void setNodename(String nodename) {
        this.nodename = nodename;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getInternalIp() {
        return this.internalIp;
    }

    public void setInternalIp(String internalIp) {
        this.internalIp = internalIp;
    }

    public String getExternalIp() {
        return this.externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }
}
