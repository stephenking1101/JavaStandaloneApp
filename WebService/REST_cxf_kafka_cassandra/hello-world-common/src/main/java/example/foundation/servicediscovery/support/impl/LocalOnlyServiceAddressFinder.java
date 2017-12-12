package example.foundation.servicediscovery.support.impl;

import org.apache.cxf.common.util.StringUtils;

import example.foundation.servicediscovery.BasicCache;
import example.foundation.servicediscovery.support.util.LocalAddressHelper;
import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;
import example.foundation.servicediscovery.support.util.SystemInfoUtil;

import java.util.Arrays;
import java.util.List;

public class LocalOnlyServiceAddressFinder extends AbstractServiceAddressFinder{
    private static final long ONE_SEC = 1000;
    private static final long CACHE_TIMEOUT = 10 * 60 * ONE_SEC;
    private static final int CACHE_SIZE = 100;
    private static BasicCache<String, String> localServiceUriCache;

    private static BasicCache<String, String> getLocalServiceUriCache(){
        if (localServiceUriCache == null){
            if (SystemInfoUtil.isTestMode()){
                localServiceUriCache = new BasicCache<String, String>(ONE_SEC, CACHE_SIZE);
            }else {
                localServiceUriCache = new BasicCache<String, String>(CACHE_TIMEOUT, CACHE_SIZE);
            }
        }
        return localServiceUriCache;
    }

    @Override
    public String getURI(String serviceName) {
        return getLocalURI(serviceName);
    }

    private String getLocalURI(String serviceName){
        String uri = getLocalServiceUriCache().get(serviceName);
        if (uri == null){
            Service service = getLocalService(serviceName);
            if (service != null){
                String serviceUri = service.getAttribute(ServiceHelper.SERVICE_URI_PROPERTY_NAME);
                String prefixUri = service.getPrefix_URI();
                prefixUri = LocalAddressHelper.covertToLocalIpUri(prefixUri);
                uri = ServiceHelper.replaceServiceUri(serviceUri, prefixUri);
                getLocalServiceUriCache().put(serviceName, uri);
            }
        }
        return uri;
    }

    protected Service getLocalService(String serviceName){
        // Return local active service if existed
        // Otherwise, return inactive local service
        // Otherwise, return null;
        Service instance = null;
        Service inactiveInstance = null;
        List<Service> allInstances = getSdFactory().getAllInstances(serviceName);
        if (allInstances == null || allInstances.isEmpty()) {
            return instance;
        }

        for (Service service : allInstances){
            if (ServiceHelper.isLocalService(service)){
                if (ServiceHelper.isActive(service)){
                    instance = service;
                }else{
                    inactiveInstance = service;
                }
            }
        }

        if (instance == null){
            instance = inactiveInstance;
        }
        return instance;
    }

    @Override
    public List<String> getAllActivateURIs(String serviceName) {
        // Return Local Service Only
        String localUri = getLocalURI(serviceName);
        if (!StringUtils.isEmpty(localUri)){
            return Arrays.asList(localUri);
        }
        return Arrays.asList();
    }
}
