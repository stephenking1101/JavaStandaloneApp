package example.servicediscovery.backend;

import java.util.ArrayList;

public class ConsulService {

    private String node;

    private String address;

    private String serviceID;

    private String serviceName;

    private ArrayList<String> serviceTags;

    private String serviceAddress;

    private int    servicePort;


    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ArrayList<String> getServiceTags() {
        return serviceTags;
    }

    public void setServiceTags(ArrayList<String> serviceTags) {
        this.serviceTags = serviceTags;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }
}


