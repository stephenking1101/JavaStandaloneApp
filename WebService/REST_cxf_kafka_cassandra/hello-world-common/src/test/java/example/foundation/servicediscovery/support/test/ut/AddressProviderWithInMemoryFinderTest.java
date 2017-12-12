package example.foundation.servicediscovery.support.test.ut;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import example.foundation.servicediscovery.support.AddressProvider;
import example.foundation.servicediscovery.support.test.AutoPorts;
import example.foundation.servicediscovery.support.test.SDMocker;
import example.foundation.servicediscovery.support.util.ConfigurationException;
import example.foundation.servicediscovery.support.util.NoServiceException;
import example.foundation.servicediscovery.support.util.ServiceHelper;

public class AddressProviderWithInMemoryFinderTest {
    private static final SDMocker.MockedService SERVICE = SDMocker.Test_Service;
    private final int port = AutoPorts.getNextPort();

    @After
    public void cleanup(){
        SERVICE.deregister();
    }

    @Test
    public void testGetAddress() {
        SERVICE.register(port, true);
        AddressProvider provider = new AddressProvider(SERVICE.getServiceName());
        Assert.assertEquals(SERVICE.getServiceUriFormat().replace(ServiceHelper.SERVICE_NAME_KEY, "localhost:" + port), provider.getAddress());
    }
    
    @Test
    public void testGetAddress_NoAddress() {
        SDMocker.getSD().putLocalService(SERVICE.getServiceName(), port, null);
        AddressProvider provider = new AddressProvider(SERVICE.getServiceName());
        Assert.assertEquals("localhost:" + port, provider.getAddress());
    }

    @Test
    public void testGetAddress_NoServiceName() {
        AddressProvider provider = new AddressProvider();
        Assert.assertEquals(null, provider.getAddress());
    }
    
    @Test
    public void testGetAddress_NoServiceNameAndAddress() {
        AddressProvider provider = new AddressProvider();
        Assert.assertEquals(null, provider.getAddress());
    }
    
    @Test
    public void testGetAddress_SD_NoServiceException() {
        SERVICE.register(port, false);
        AddressProvider provider = new AddressProvider(SERVICE.getServiceName());
        
        try{
            provider.getAddress();
        }catch (NoServiceException e){
            Assert.assertEquals(SERVICE.getServiceName(), e.getServiceName());
        }
    }
    
    @Test
    public void testGetAddress_SD_ConfigurationException() {
        AddressProvider provider = new AddressProvider(SERVICE.getServiceName());
        
        try{
            provider.getAddress();
        }catch (ConfigurationException e){
            Assert.assertEquals(SERVICE.getServiceName(), e.getServiceName());
        }
    }
}
