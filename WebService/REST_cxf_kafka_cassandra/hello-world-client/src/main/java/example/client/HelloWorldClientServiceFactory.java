package example.client;

import example.client.api.HelloWorldServiceClient;
import example.foundation.servicediscovery.AbstractSpringServiceFactory;

public class HelloWorldClientServiceFactory extends AbstractSpringServiceFactory {
    @Override
    protected String getContextFile() {
        return "classpath*:/applicationContext_HelloWorldClient.xml";
    }

    @Override
    public Class[] getSupportedServices() {
        return new Class[] { HelloWorldServiceClient.class };
    }
}
