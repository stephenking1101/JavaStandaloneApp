package example.foundation.servicediscovery.support.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.common.util.StringUtils;

import example.foundation.servicediscovery.BasicCache;

public class SdFactory {
    private static final long ONE_SEC = 1000;
    private static final long CACHE_TIMEOUT = 10 * 60 * ONE_SEC;
    private static final int CACHE_SIZE = 100;
    private static BasicCache<String, ServiceUriInstanceMapping> serviceCache;
    private ServiceFactory serviceFactory = ServiceFactory.getInstance();

    static {
        SdLbsPropertyConfig.startSync();
    }

    private static class ServiceUriInstanceMapping {
        private Map<String, Service> uriMaps = new HashMap<String, Service>();
        ServiceUriInstanceMapping(List<Service> services){
            for (Service service : services){
                uriMaps.put(service.getPrefix_URI(), service);
            }
        }

        Service get(String uri){
            return uriMaps.get(uri);
        }
    }

    private static BasicCache<String, ServiceUriInstanceMapping> getServiceCache(){
        if (serviceCache == null){
            if (SystemInfoUtil.isTestMode()){
                serviceCache = new BasicCache<String, ServiceUriInstanceMapping>(ONE_SEC, CACHE_SIZE);
            }else {
                serviceCache = new BasicCache<String, ServiceUriInstanceMapping>(CACHE_TIMEOUT, CACHE_SIZE);
            }
        }
        return serviceCache;
    }

    public List<Service> getAllInstances(String serviceName){
        Service lbsInstance = SdLbsPropertyConfig.getService(serviceName);
        if (lbsInstance != null){
            return Arrays.asList(lbsInstance);
        }

        return serviceFactory.getAllInstances(serviceName);
    }

    public Service getRandomInstance(String serviceName) {
        Service instance = SdLbsPropertyConfig.getService(serviceName);
        if (instance == null){
            String uri = serviceFactory.getRandomInstanceURI(serviceName);
            instance = getInstance(serviceName, uri);
        }

        return instance;
    }

    private Service getInstance(String serviceName, String uri){
        if (StringUtils.isEmpty(uri)){
            return null;
        }

        ServiceUriInstanceMapping instancesMap = getServiceCache().get(serviceName);
        if (instancesMap == null){
            instancesMap = updateInstanceMapCache(serviceName);
        }

        Service instance = instancesMap.get(uri);
        if (instance == null){
            instancesMap = updateInstanceMapCache(serviceName);
            instance = instancesMap.get(uri);
        }
        return instance;
    }

    private ServiceUriInstanceMapping updateInstanceMapCache(String serviceName){
        ServiceUriInstanceMapping instancesMap = new ServiceUriInstanceMapping(getAllInstances(serviceName));
        getServiceCache().put(serviceName, instancesMap);
        return instancesMap;
    }

    public static void main(String[] args) throws InterruptedException {
    	//System.setProperty("HELLO_WORLD_SERVICE", "http://127.0.0.1:8080/hello-world-war");
        Thread.sleep(3000);
        for (String key: SdLbsPropertyConfig.configedLbsCache.keySet()){
            String name = key.toLowerCase();
            System.out.println("==> " + key + " = " + name + ": "
                    + ServiceHelper.getServiceUri(SdLbsPropertyConfig.getService(key.toLowerCase())));
        }
    }

    private static class SdLbsPropertyConfig{
        private static final String DEFAULT_TYPE = "SD_LBS_DEFAULT";
        private static final String SUFFIX_LBS = "_SERVICE";
        private static Map<String, Service> configedLbsCache = new HashMap<String, Service>();
        private static final Thread syncThread = new Thread(new SyncTask());

        public static void startSync(){
            syncThread.setDaemon(true);
            syncThread.start();
        }

        private static class SyncTask implements Runnable {
            private static final long SYNCUP_PERIOD = 1000; // 5 * 60 * 1000;
            @Override
            public void run() {
                do {
                    SdLbsPropertyConfig.updateProperties();
                    try {
                        Thread.sleep(SYNCUP_PERIOD);
                    } catch (InterruptedException e) {
                    }
                } while (true);
            }
        }

        private static void updateProperties(){
            Map<String, Service> tmpCache = new HashMap<String, Service>();
            Properties properties = System.getProperties();
            for (String propertyName : properties.stringPropertyNames()){
                String key = covertName(propertyName);

                if (!key.endsWith(SUFFIX_LBS)){
                    continue;
                }

                String serviceName = key.substring(0,key.indexOf(SUFFIX_LBS));
                String propertyValue = properties.getProperty(propertyName).trim();
                if (!StringUtils.isEmpty(propertyValue)){
                    Service lbs = new Service(serviceName, DEFAULT_TYPE,
                            propertyValue, Service.STATUS.ACTIVE);
                    tmpCache.put(serviceName, lbs);
                }
            }
            configedLbsCache = tmpCache;
        }

        public static Service getService(String serviceName){
            if (StringUtils.isEmpty(serviceName)){
                return null;
            }
            String key = covertName(serviceName);
            return configedLbsCache.get(key);
        }

        public static boolean containsService(String serviceName){
            if (StringUtils.isEmpty(serviceName)){
                return false;
            }
            String key = covertName(serviceName);
            return configedLbsCache.containsKey(key);
        }

        private static String covertName(String serviceName){
            return serviceName.trim().replace('-', '_').toUpperCase();
        }
    }
}
