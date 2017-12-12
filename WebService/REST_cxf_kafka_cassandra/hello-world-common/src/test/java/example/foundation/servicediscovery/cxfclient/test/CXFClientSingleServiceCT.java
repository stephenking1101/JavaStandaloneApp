package example.foundation.servicediscovery.cxfclient.test;

import org.junit.Assert;
import org.junit.Test;

import example.foundation.servicediscovery.strategy.delegate.FakeServer;

public class CXFClientSingleServiceCT extends AbstractCXFClientTest{
    @Test
    public void testCreateClientFirstSuccess(){
        FakeServer client = TestServices.getServiceClient();
        TestServices.startService();

        String rc = client.toLower(TEST_PARMS);
        Assert.assertEquals(TEST_PARMS.toLowerCase(), rc);
    }

    @Test
    public void testCreateServiceFirstSuccess(){
        TestServices.startService();
        FakeServer client = TestServices.getServiceClient();
        String rc = client.toLower(TEST_PARMS);
        Assert.assertEquals(TEST_PARMS.toLowerCase(), rc);
    }

    @Test
    public void testNoServiceRegistered(){
        FakeServer client = TestServices.getServiceClient();
        SERVICE.deregister();

        try {
            String rc = client.toLower(TEST_PARMS);
            Assert.fail();
        }catch(Exception e){
        }
    }

    @Test
    public void testNoServiceAvailable(){
        TestServices.startService();
        TestServices.stopAllServices();
        FakeServer client = TestServices.getServiceClient();

        try {
            String rc = client.toLower(TEST_PARMS);
            Assert.fail();
        }catch(Exception e){
        }
    }
}
