package example.foundation.servicediscovery;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractServiceFactory implements ServiceFactory {

    @Override
    public Map<String, String> getSupportedCriteria() {
        return new HashMap<String, String>();
    }
}
