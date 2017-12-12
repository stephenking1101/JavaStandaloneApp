package example.foundation.servicediscovery.support.util;

import java.util.List;

public interface Backend {

    public Service getOneActiveInstance(String serviceName);

    public boolean isActive(String serviceId);

    public List<Service> getAllByName(String serviceName);

}
