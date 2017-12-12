package example.foundation.servicediscovery.cxfclient.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import example.foundation.servicediscovery.strategy.delegate.FakeServer;
import example.foundation.servicediscovery.support.test.AutoPorts;
import example.foundation.servicediscovery.support.test.SDMocker;

public class CXFClientSanityTest extends AbstractCXFClientTest{
    private static FakeServer client = TestServices.getServiceClient();

    @Before
    public void createService(){
        TestServices.startService();
    }

    @Test
    public void testClientSuccessAndLBSInvoked(){
        SDMocker.getSD().invokeCounts.clear();

        String rc = client.toLower(TEST_PARMS);
        Assert.assertEquals(TEST_PARMS.toLowerCase(), rc);

        Assert.assertEquals(1, SDMocker.getSD().invokeCounts.getRandomURI.get());
        Assert.assertEquals(0, SDMocker.getSD().invokeCounts.getAllActivate.get());
    }

    @Test
    public void testFailoverSuccess(){
        // Register severail unavailable port
        for (int i=0; i<2; i++){
            SERVICE.register(AutoPorts.getNextPort(), true);
        }

        SDMocker.getSD().invokeCounts.clear();
        do {
            client.sayHello(TEST_PARMS);
        }while(SDMocker.getSD().invokeCounts.getAllActivate.get() < 1);
    }
}
