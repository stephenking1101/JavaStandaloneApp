package example.configurationservice.local.factory;

import example.configurationservice.ConfigurationService;
import example.configurationservice.local.dao.impl.MonitorableRepository;
import example.foundation.servicediscovery.AbstractSpringServiceFactory;

public class ConfigurationLocalServiceFactory extends AbstractSpringServiceFactory {

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getSupportedServices() {
        return new Class[] { ConfigurationService.class, MonitorableRepository.class };
    }

    @Override
    protected String getContextFile() {
        return "classpath*:/applicationContext_ConfigurationLocalService.xml";
    }

}
