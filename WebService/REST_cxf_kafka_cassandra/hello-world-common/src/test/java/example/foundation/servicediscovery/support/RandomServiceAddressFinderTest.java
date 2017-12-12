package example.foundation.servicediscovery.support;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.ServiceHelper;

public class RandomServiceAddressFinderTest extends AbstractServiceAddressFinderTest{
    private String serviceName = "Random-Test";
    private String servicePrefix = "192.168.0.10:2700";
    private String serviceUri = "http://" + ServiceHelper.SERVICE_NAME_KEY + "/" + serviceName;
    private final String randomServiceUrl = serviceUri.replace(ServiceHelper.SERVICE_NAME_KEY, servicePrefix);
    @Before
    public void mockRandomType(){
        mockFinderType(ServiceAddressFinderAdapter.FINDER_TYPE_RANDOM);
        mockSdFactory();
    }

    @Test
    public void testGetUri(){
        String url = finderAdapter.getURI(serviceName);
        Assert.assertEquals(randomServiceUrl, url);
    }

    @Test
    public void testGetUriWhenServiceNameNonExistent() {
        String url = finderAdapter.getURI(serviceNameNonExistent);
        Assert.assertNull(url);
    }

    @Test
    public void testGetAllActiveUris(){
        List<String> urls = finderAdapter.getAllActivateURIs(serviceName);
        Assert.assertEquals(INJECT_ACTIVE_SERVICE_COUNT + 1, urls.size());
    }
;
    @Test
    public void testGetAllActiveUrisWhenServiceNameNonExistent(){
        List<String> urls = finderAdapter.getAllActivateURIs(serviceNameNonExistent);
        Assert.assertEquals(0, urls.size());
    }

    @Test
    public void testGetUriWhenBackEndReturnNull() {
        mockSdFacotyrAllInstances("serviceNameForNullList", null);
        Assert.assertEquals(0, finderAdapter.getAllActivateURIs("serviceNameForNullList").size());
        Assert.assertNull(finderAdapter.getURI("serviceNameForNullList"));
    }

    private void mockSdFactory(){
        Service service = newService(serviceName, servicePrefix, serviceUri);
        mockSdFactoryRandomInstance(service);

        List<Service> services = new LinkedList<Service>();
        services.add(service);
        injectServiceInstace(serviceName, services);
        mockSdFacotyrAllInstances(serviceName, services);
    }
}
