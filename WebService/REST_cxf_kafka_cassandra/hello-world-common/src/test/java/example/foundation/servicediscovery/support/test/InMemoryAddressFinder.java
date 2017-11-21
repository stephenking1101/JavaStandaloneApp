package example.foundation.servicediscovery.support.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import example.foundation.servicediscovery.Service;
import example.foundation.servicediscovery.Service.STATUS;
import example.foundation.servicediscovery.support.ServiceAddressFinder;
import example.foundation.servicediscovery.support.ServiceHelper;

public class InMemoryAddressFinder implements ServiceAddressFinder {    
    private AddressCache sd = new AddressCache();
    public methodInvokeCounts invokeCounts = new methodInvokeCounts();

    public static class methodInvokeCounts{
        public AtomicInteger getRandomURI = new AtomicInteger();
        public AtomicInteger getStickyURI = new AtomicInteger();
        public AtomicInteger getRandomAsSticky = new AtomicInteger();
        public AtomicInteger getAllActivate = new AtomicInteger();

        public void clear(){
            getRandomURI.set(0);
            getStickyURI.set(0);
            getRandomAsSticky.set(0);
            getAllActivate.set(0);
        }
    }

    @Override
    public String getURI(String serviceName) {
        invokeCounts.getRandomURI.incrementAndGet();
        return sd.getRandomURI(serviceName);
    }

    @Override
    public List<String> getAllActivateURIs(String serviceName) {
        invokeCounts.getAllActivate.incrementAndGet();
        List<String> addresses = new ArrayList<String>();
        for (Service service:sd.getAll(serviceName)){
            if (service.getStatus() == STATUS.ACTIVE){
                addresses.add(ServiceHelper.getServiceUri(service));
            }
        }
        return addresses;
    }

    public List<Service> getAll(String serviceName) {
        invokeCounts.getAllActivate.incrementAndGet();
        return sd.getAll(serviceName);
    }

    public void put(Service service){
        sd.put(service);
    }
    
    public void putLocalService(String name, int port, String serviceUri){
        putService(name, "localhost:" + port, serviceUri);
    }

    public void putLocalService(String name, int port, String serviceUri, STATUS status){
        putService(name, "localhost:" + port, serviceUri, status);
    }

    public void putService(String name, String prefixUri, String serviceUri){
        putService(name, prefixUri, serviceUri, STATUS.ACTIVE);
    }

    public void putService(String name, String prefixUri, String serviceUri, STATUS status){
        putService(name, name + "_" + prefixUri, prefixUri, serviceUri, status);
    }
    
    public void putService(String name, String id, String prefixUri, String serviceUri){
        putService(name, id, prefixUri, serviceUri, STATUS.ACTIVE);
    }
    
    public void putService(String name, String id, String prefixUri, String serviceUri, STATUS status){
        Service service = new Service(name, "INMEMORY-SERVICE", prefixUri, status);
        service.setAttribute(ServiceHelper.SERVICE_URI_PROPERTY_NAME, serviceUri);
        service.setId(id);
        sd.put(service);
    }
    
    public void remove(Service service){
        sd.remove(service);
    }
    
    public void remove(String serviceName){
        sd.remove(serviceName);
    }
    
    public void clear(){
        sd.clear();
    }
}
