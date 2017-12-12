package example.foundation.servicediscovery.cxfclient.test;

import org.junit.Assert;
import org.junit.Test;

import example.foundation.servicediscovery.strategy.delegate.FakeServer;
import example.foundation.servicediscovery.support.test.SDMocker;

public class CXFClientLBSServiceCT extends AbstractCXFClientTest{
    @Test
    public void testLBSInvoked(){
        TestServices.startService();
        TestServices.startService();
        TestServices.startService();

        FakeServer client = TestServices.getServiceClient();

        SDMocker.getSD().invokeCounts.clear();
        int executeCount = 3;
        for (int i=0; i<executeCount; i++) {
            client.toLower(TEST_PARMS);
            client.sayHello(TEST_PARMS);
        }

        Assert.assertEquals(executeCount*2, SDMocker.getSD().invokeCounts.getRandomURI.get());
        Assert.assertEquals(0, SDMocker.getSD().invokeCounts.getAllActivate.get());
    }
}