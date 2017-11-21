package example.foundation.servicediscovery;

import java.util.List;
import java.util.Map;

public interface ServiceDiscovery {
    /**
     * Finds a service implementing a given service.
     * 
     * @param serviceClass
     *            - The class to discover.
     * @param discoveryCritera
     *            - Optional criteria to be used during discovery of the
     *            service. When multiple matches are found, the first match is
     *            returned where 'first' is determined is dependent on the
     *            implementation.
     * @param serviceConfig
     *            - Optional configuration passed to the service.
     * @return The first resolved Service of the type indicated by
     *         messagingServiceClass that matched the discovery criteria.
     */
    <T> T discover(Class<T> serviceClass, Map<String, String> discoveryCritera, Map<String, Object> serviceConfig);

    /**
     * Finds all services implementing a given interface.
     *
     * @param serviceClass
     *            - The class to discover.
     * @param discoveryCritera
     *            - Optional criteria to be used during discovery of the
     *            service. When multiple matches are found, the first match is
     *            returned where 'first' is determined is dependent on the
     *            implementation.
     * @param serviceConfig
     *            - Optional configuration passed to the service.
     * @return A list of services that were found. Always non-null. In case no
     *         services are found, an empty list is returned.
     */
    <T> List<T> discoverAll(Class<T> serviceClass, Map<String, String> discoveryCritera,
            Map<String, Object> serviceConfig);

}
