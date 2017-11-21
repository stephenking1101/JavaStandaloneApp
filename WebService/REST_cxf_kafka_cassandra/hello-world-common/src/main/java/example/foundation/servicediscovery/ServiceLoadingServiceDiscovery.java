package example.foundation.servicediscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class ServiceLoadingServiceDiscovery.
 */
public class ServiceLoadingServiceDiscovery implements ServiceDiscovery {

    /** The log. */
    private static Logger logger = LoggerFactory.getLogger(ServiceLoadingServiceDiscovery.class.getName());

    /** The loader. */
    private ServiceLoader<ServiceFactory> itsLoader;

    private List<ServiceFactory> factoryCache;

    /**
     * Instantiates a new static linked messaging discovery.
     */
    public ServiceLoadingServiceDiscovery() {
        itsLoader = ServiceLoader.load(ServiceFactory.class, this.getClass().getClassLoader());
    }

    /**
     * Instantiates a new static linked messaging discovery. Using the
     * classloader provided
     */
    public ServiceLoadingServiceDiscovery(ClassLoader classloader) {
        itsLoader = ServiceLoader.load(ServiceFactory.class, classloader);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.jee.ngin.messaging.adapter.MessagingDiscovery#
     * discover(java. lang.Class, java.util.Map)
     */
    @Override
    public <T> T discover(Class<T> serviceClass, Map<String, String> critera, Map<String, Object> config) {
        T result = null;

        final List<ServiceFactory> factories = getFactoryCache();

        for (ServiceFactory serviceFactory : factories) {
            if (logger.isDebugEnabled()) {
                logger.debug("discover, found possible matching service factory: {}", serviceFactory);
            }

            if (match(serviceFactory, serviceClass, critera)) {
                Map<String, Object> tmp = new HashMap<String, Object>(5);

                if (config != null) {
                    tmp.putAll(config);
                }

                result = serviceFactory.instance(serviceClass, tmp);

                if (logger.isDebugEnabled()) {
                    logger.debug("discover, found matching service factory: {}", serviceFactory);
                }

                if (result != null) {
                    break;
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("discover, service factory does not match: {}", serviceFactory);
                }
            }
        }
        return result;
    }

    @Override
    public <T> List<T> discoverAll(Class<T> serviceClass, Map<String, String> critera, Map<String, Object> config) {
        List<T> results = new ArrayList<T>();

        final List<ServiceFactory> factories = getFactoryCache();

        for (ServiceFactory serviceFactory : factories) {
            if (logger.isDebugEnabled()) {
                logger.debug("discover, found possible matching service factory: {}", serviceFactory);
            }

            if (match(serviceFactory, serviceClass, critera)) {
                Map<String, Object> tmp = new HashMap<String, Object>(5);

                if (config != null) {
                    tmp.putAll(config);
                }

                final T result = serviceFactory.instance(serviceClass, tmp);

                if (logger.isDebugEnabled()) {
                    logger.debug("discover, found matching service factory: {}", serviceFactory);
                }

                if (result != null) {
                    results.add(result);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("discover, service factory does not match: {}", serviceFactory);
                }
            }
        }
        return results;
    }

    /**
     * Match. Checks if there is a match between the desired and
     * discovered service.
     * 
     * @param serviceFactory the service
     * @param serviceClass the messaging service class
     * @param critera the discovery criteria
     * 
     * @return true, if successful
     */
    @SuppressWarnings("rawtypes")
    private <T> boolean match(ServiceFactory serviceFactory, Class<T> serviceClass, Map<String, String> critera) {
        boolean match = false;
        Map<String, String> supportedCritera = serviceFactory.getSupportedCriteria();

        for (Class service : serviceFactory.getSupportedServices()) {
            if (!service.equals(serviceClass)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("service does not match: {}", service);
                }
                continue;
            }

            if (SvcDiscoUtils.isEmpty(critera) && SvcDiscoUtils.isEmpty(supportedCritera)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("no critera requried, and no supported critera defined for service factory");
                }
                match = true;
                break;
            }

            if (SvcDiscoUtils.isNotEmpty(critera)) {
                for (String criteraKey : critera.keySet()) {
                    String supportedValue = SvcDiscoUtils.getString(supportedCritera, criteraKey);
                    if (SvcDiscoUtils.isBlank(supportedValue) || !supportedValue.equals(critera.get(criteraKey))) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("critera does not match: {}", criteraKey);
                        }
                        match = false;
                        break;
                    }
                    match = true;
                }
            }
        }
        return match;
    }

    private List<ServiceFactory> getFactoryCache() {
        if (factoryCache == null) {
            synchronized (itsLoader) {
                if (factoryCache == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("discover, using loader: {}", itsLoader);
                    }
                    factoryCache = new ArrayList<ServiceFactory>();
                    for (ServiceFactory serviceFactory : itsLoader) {
                        factoryCache.add(serviceFactory);
                    }
                }
            }
        }
        return factoryCache;
    }
}
