package example.foundation.servicediscovery.support;

import java.util.List;

import example.foundation.servicediscovery.ServiceDiscoveryFactory;

public class AddressProvider {
    private String serviceName = null;

    public AddressProvider(String serviceName){
        setServiceName(serviceName);
    }

    public AddressProvider(){
    }

    public String getServiceName(){
        return serviceName;
    }

    public void setServiceName(String serviceName){
        if (serviceName!=null && !serviceName.isEmpty()) {
            this.serviceName = serviceName;
        }
    }

    public List<String> getFailoverAddresses(String excludedAddress){
        if (getServiceName() == null) {
            return null;
        }
        List<String> addresses = getAddressFinder().getAllActivateURIs(getServiceName());
        addresses.remove(excludedAddress);
        return addresses;
    }
    
    public String getAddress() {
        if (getServiceName() == null) {
            return null;
        }
        return getAddressFinder().getURI(getServiceName());
    }

    private static ServiceAddressFinder getAddressFinder(){
        return ServiceDiscoveryFactory.getServiceDiscovery()
                .discover(ServiceAddressFinder.class, null, null);
    }
}