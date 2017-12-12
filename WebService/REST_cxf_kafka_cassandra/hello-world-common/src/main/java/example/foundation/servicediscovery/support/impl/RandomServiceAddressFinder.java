package example.foundation.servicediscovery.support.impl;

import java.util.ArrayList;
import java.util.List;

import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;

public class RandomServiceAddressFinder extends AbstractServiceAddressFinder {
    @Override
    public String getURI(String serviceName) {
        Service service = getRandomService(serviceName);
        return ServiceHelper.getServiceUri(service);
    }

    @Override
    public List<String> getAllActivateURIs(String serviceName) {
        // Return All Activate Service By Default
        List<String> addresses = new ArrayList<String>();
        List<Service> allInstances = getSdFactory().getAllInstances(serviceName);
        if (allInstances == null || allInstances.isEmpty()) {
            return addresses;
        }
        for (Service service : allInstances){
            if (ServiceHelper.isActive(service)){
                addresses.add(ServiceHelper.getServiceUri(service));
            }
        }
        return addresses;
    }
}
