package example.foundation.servicediscovery.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.BasicCache;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.foundation.servicediscovery.support.impl.LocalFirstServiceAddressFinder;
import example.foundation.servicediscovery.support.impl.LocalOnlyServiceAddressFinder;
import example.foundation.servicediscovery.support.impl.RandomServiceAddressFinder;
import example.foundation.servicediscovery.support.util.SystemInfoUtil;

public class ServiceAddressFinderAdapter implements ServiceAddressFinder {
    public static final String FINDER_TYPE_RANDOM = "random";
    public static final String FINDER_TYPE_LOCAL_FIRST = "localfirst";
    public static final String FINDER_TYPE_LOCAL_ONLY = "localonly";

    public static final String FINDER_TYPE_KEY = "sd.lbs.strategy";
    public static final String DEFAULT_FINDER_TYPE = FINDER_TYPE_RANDOM;
    private static final long ONE_SEC = 1000;
    private static final long CACHE_TIMEOUT = 10 * 60 * ONE_SEC;
    private static final int CACHE_SIZE = 100;
    private static BasicCache<String, ServiceAddressFinder> addressFinderCache;
    private static BasicCache<String, ServiceAddressFinder> getAddressFinderCache(){
        if (addressFinderCache == null){
            if (SystemInfoUtil.isTestMode()){
                addressFinderCache = new BasicCache<String, ServiceAddressFinder>(ONE_SEC, CACHE_SIZE);
            }else {
                addressFinderCache = new BasicCache<String, ServiceAddressFinder>(CACHE_TIMEOUT, CACHE_SIZE);
            }
        }
        return addressFinderCache;
    }
    private static final Map<String, ServiceAddressFinder> FinderMap = new HashMap<String, ServiceAddressFinder>();
    static {
        FinderMap.put(FINDER_TYPE_LOCAL_ONLY, new LocalOnlyServiceAddressFinder());
        FinderMap.put(FINDER_TYPE_LOCAL_FIRST, new LocalFirstServiceAddressFinder());
        FinderMap.put(FINDER_TYPE_RANDOM, new RandomServiceAddressFinder());
    }

    private static String getFinderType(){
        ConfigurationService configurationService = ServiceDiscoveryFactory.getServiceDiscovery()
                .discover(ConfigurationService.class, null, null);
        if (configurationService != null){
            String type = configurationService.getString(FINDER_TYPE_KEY, DEFAULT_FINDER_TYPE).trim().toLowerCase();
            if (FinderMap.containsKey(type)){
                return type;
            }
        }
        return DEFAULT_FINDER_TYPE;
    }
    private static ServiceAddressFinder getFinder(){
        ServiceAddressFinder finder = getAddressFinderCache().get(FINDER_TYPE_KEY);
        if (finder == null){
            finder = FinderMap.get(getFinderType());
            getAddressFinderCache().put(FINDER_TYPE_KEY, finder);
        }
        return finder;
    }

    public static ServiceAddressFinder getFinder(String type){
        return FinderMap.get(type.trim().toLowerCase());
    }

    @Override
    public String getURI(String serviceName) {
        return getFinder().getURI(serviceName);
    }

    @Override
    public List<String> getAllActivateURIs(String serviceName) {
        return getFinder().getAllActivateURIs(serviceName);
    }
}
