package example.foundation.servicediscovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDiscoveryDelegate implements ServiceDiscovery {

    private ServiceDiscovery globalDiscovery;

    private ServiceDiscovery localDiscovery;

    static Map<Class<?>, Object> mocks = new HashMap<Class<?>, Object>();

    ServiceDiscoveryDelegate() {
        this(ServiceDiscoveryDelegate.class.getClassLoader());
    }

    ServiceDiscoveryDelegate(ClassLoader classLoader) {
        localDiscovery = new ServiceLoadingServiceDiscovery(classLoader);
        globalDiscovery = new ServiceLoadingServiceDiscovery(ServiceDiscoveryDelegate.class.getClassLoader());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T discover(Class<T> serviceClass, Map<String, String> criteria, Map<String, Object> config) {
        if (mocks.containsKey(serviceClass)) {
            return (T) mocks.get(serviceClass);
        }
        T service = localDiscovery.discover(serviceClass, criteria, config);
        if (service != null) {
            return service;
        }
        return globalDiscovery.discover(serviceClass, criteria, config);
    }

    @Override
    public <T> List<T> discoverAll(Class<T> serviceClass, Map<String, String> criteria, Map<String, Object> config) {
        List<T> services = localDiscovery.discoverAll(serviceClass, criteria, config);
        services.addAll(globalDiscovery.discoverAll(serviceClass, criteria, config));
        return services;
    }
}
