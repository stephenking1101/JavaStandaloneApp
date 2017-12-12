package example.foundation.servicediscovery.support.factory;

import example.foundation.servicediscovery.AbstractSpringServiceFactory;
import example.foundation.servicediscovery.support.ServiceAddressFinder;

public class ServiceSupportAddressFinderFactory extends AbstractSpringServiceFactory {

	@Override
	public Class<?>[] getSupportedServices() {
		return new Class[] { ServiceAddressFinder.class };
	}

	@Override
	protected String getContextFile() {
		return "classpath*:/applicationContext_ServiceAddressFinder.xml";
	}

}
