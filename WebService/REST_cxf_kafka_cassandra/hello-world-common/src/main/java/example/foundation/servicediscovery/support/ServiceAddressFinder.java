package example.foundation.servicediscovery.support;

import java.util.List;

public interface ServiceAddressFinder {
    String getURI(String serviceName);
    List<String> getAllActivateURIs(String serviceName);
}
