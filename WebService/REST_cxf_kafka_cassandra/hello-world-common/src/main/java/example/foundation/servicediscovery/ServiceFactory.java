package example.foundation.servicediscovery;

import java.util.Map;

public interface ServiceFactory {

    /**
     * Get a list of supported services.
     * 
     * @return - Array of supported service (interface) classes
     */
    @SuppressWarnings({ "rawtypes" })
    Class[] getSupportedServices();

    /**
     * This method is called to create an instance of the service.
     * 
     * @param serviceClass - The to be supported service (interface)
     *            class
     * @param configuration - Configuration as indicated by the user
     *            of the service that it wants to be applied on the
     *            instance.
     * @return T - A new instance of the type serviceClass, or null if
     *         a problem occured during instantiation.
     */
    <T> T instance(Class<T> serviceClass, Map<String, Object> configuration);

    /**
     * Get all the supported critera which are in string key-value
     * pairs
     * 
     * @return - Map of supported criteria
     */
    Map<String, String> getSupportedCriteria();
}
