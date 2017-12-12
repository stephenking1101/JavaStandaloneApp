package example.foundation.servicediscovery.support;

import org.apache.cxf.clustering.FailoverFeature;
import org.apache.cxf.clustering.SdLbsTargetSelector;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;

public class CXFClientHelper {
    public static final String DEFAULT_ADDRESS_PLACEHOLDER = "http://service.address/unused";

    public static JAXRSClientFactoryBean getClientFactory(AddressProvider addressProvider){
        JAXRSClientFactoryBean clientFactory = new JAXRSClientFactoryBean();

        clientFactory.setAddress(DEFAULT_ADDRESS_PLACEHOLDER);

        SdLbsTargetSelector targetSelector = new SdLbsTargetSelector();
        targetSelector.setAddressProvider(addressProvider);

        FailoverFeature lbsFeature = new FailoverFeature();
        lbsFeature.setTargetSelector(targetSelector);
        clientFactory.getFeatures().add(lbsFeature);
        return clientFactory;
    }
}
