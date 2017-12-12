package example.foundation.servicediscovery.support.impl;

import java.util.ArrayList;
import java.util.List;

import example.foundation.servicediscovery.support.util.LocalAddressHelper;
import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;

import org.apache.cxf.common.util.StringUtils;

public class LocalFirstServiceAddressFinder extends LocalOnlyServiceAddressFinder{
    @Override
    public String getURI(String serviceName) {
        String uri = null;
        Service service = getLocalService(serviceName);
        if (service != null && service.getStatus().equals(Service.STATUS.ACTIVE)){
            String serviceUri = service.getAttribute(ServiceHelper.SERVICE_URI_PROPERTY_NAME);
            String prefixUri = service.getPrefix_URI();
            prefixUri = LocalAddressHelper.covertToLocalIpUri(prefixUri);
            uri = ServiceHelper.replaceServiceUri(serviceUri, prefixUri);
        }

        if (StringUtils.isEmpty(uri)){
            uri = ServiceHelper.getServiceUri(getRandomService(serviceName));
        }
        return uri;
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
