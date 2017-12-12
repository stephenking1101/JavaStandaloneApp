package example.foundation.servicediscovery.support.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import example.foundation.servicediscovery.support.util.ConfigurationException;
import example.foundation.servicediscovery.support.util.NoServiceException;
import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;

class AddressCache {
    private final HashMap<String, ArrayList<Service>> allServices = 
            new HashMap<String, ArrayList<Service>>();
    private final HashMap<String, ArrayList<Service>> activeServices = 
            new HashMap<String, ArrayList<Service>>();

    private static Random random = new Random();
    
    private static void removeService(HashMap<String, ArrayList<Service>> services, 
            String serviceName, String serviceId){
        if (!services.containsKey(serviceName)){
            services.put(serviceName, new ArrayList<Service>());
            return;
        }
        
        ArrayList<Service> serviceArr = services.get(serviceName);
        for (int idx=0; idx< serviceArr.size(); idx++){
            if (serviceArr.get(idx).getId().equals(serviceId)){
                serviceArr.remove(idx);
                return;
            }
        }
    }
    
    synchronized void put(Service service){
        remove(service);
        allServices.get(service.getName()).add(service);
        if (service.getStatus() == Service.STATUS.ACTIVE){
            activeServices.get(service.getName()).add(service);
        }
    }
    
    synchronized void remove(Service service){
        removeService(allServices, service.getName(), service.getId());
        removeService(activeServices, service.getName(), service.getId());
    }
    
    synchronized void remove(String serviceName){
        allServices.remove(serviceName);
        activeServices.remove(serviceName);
    }
    
    private synchronized Service getRandomService(String serviceName){
        if (!allServices.containsKey(serviceName) || allServices.get(serviceName).isEmpty()){
            throw new ConfigurationException("missing LBS env configuration and discovery adapter",
                    serviceName);
        }
        
        if (!activeServices.containsKey(serviceName) || activeServices.get(serviceName).isEmpty()){
            throw new NoServiceException("Cannot get any instance", serviceName);
        }
        
        ArrayList<Service> srvList = activeServices.get(serviceName);
        int size = srvList.size();
        int offset = random.nextInt(size);
        return srvList.get(offset);
    }

    synchronized String getRandomURI(String serviceName) {
        return ServiceHelper.getServiceUri(getRandomService(serviceName));
    }

    @SuppressWarnings("unchecked")
    synchronized List<Service> getAll(String serviceName) {
        if (!allServices.containsKey(serviceName) || allServices.get(serviceName).isEmpty()){
            throw new ConfigurationException("missing LBS env configuration and discovery adapter",
                    serviceName);
        }
        return (ArrayList<Service>) allServices.get(serviceName).clone();
    }

    public synchronized void clear() {
        allServices.clear();
        activeServices.clear();
    }
}
