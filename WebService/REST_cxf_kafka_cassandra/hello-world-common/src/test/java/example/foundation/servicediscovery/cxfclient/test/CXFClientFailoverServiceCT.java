package example.foundation.servicediscovery.cxfclient.test;

import org.junit.Assert;
import org.junit.Test;

import example.foundation.servicediscovery.strategy.delegate.FakeServer;
import example.foundation.servicediscovery.support.test.AutoPorts;
import example.foundation.servicediscovery.support.test.SDMocker;

public class CXFClientFailoverServiceCT extends AbstractCXFClientTest{
    /**
     * Scenario:
     *    1. 2 bad instances are registered to SD as passing, but they're actually down.
     *       1 good instance is registered to SD as passing.
     *    2. client try to access the service multi-times, till failover happens
     *    3. the good instance is marked as failing
     *    4. client try to access the service
     * Expected behavior:
     *    - No access failure happens on step 2.
     *    - Client access failed and ClientWebApplicationException happens on Step4
     *      failover happened
     */
    @Test
    public void testFailoverSuccessThenFailed(){
        // Register severail unavailable port
        for (int i=0; i<2; i++){
            SERVICE.register(AutoPorts.getNextPort(), true);
        }

        // start only 1 service instance
        int port = TestServices.startService();

        FakeServer server = TestServices.getServiceClient();

        SDMocker.getSD().invokeCounts.clear();
        // service access should passed after failover happens.
        do {
            server.sayHello(TEST_PARMS);
        }while(SDMocker.getSD().invokeCounts.getAllActivate.get() < 1);

        // setSdInstanceFailing the service port
        SERVICE.register(port, false);
        SDMocker.getSD().invokeCounts.clear();
        try{
            server.sayHello(TEST_PARMS);
            Assert.fail();
        }catch (Exception e){
            Assert.assertEquals(1, SDMocker.getSD().invokeCounts.getAllActivate.get()); // Failover triggered.
        }
    }

    /**
     * Scenario:
     *    1. 2 bad instances are registered to SD as passing, but they're actually down.
     *    2. client try to access the service
     *    3. 1 good instance is registered to SD as passing.
     *    4. client try to access the service multi-times, till failover happens
     * Expected behavior:
     *    - Client access failed and ClientWebApplicationException happens on Step2
     *      failover happened
     *    - No access failure happens on step 4.
     */
    @Test
    public void testFailoverFailedThenSuccess(){
        // Register severail unavailable port
        for (int i=0; i<2; i++){
            SERVICE.register(AutoPorts.getNextPort(), true);
        }

        FakeServer server = TestServices.getServiceClient();

        // Call server, failover will happens here
        // Service access failed.
        SDMocker.getSD().invokeCounts.clear();
        try{
            server.sayHello(TEST_PARMS);
            Assert.fail();
        }catch (Exception e){
            Assert.assertEquals(1, SDMocker.getSD().invokeCounts.getAllActivate.get()); // Failover triggered.
        }

        // start only 1 service instance
        TestServices.startService();

        SDMocker.getSD().invokeCounts.clear();
        // service access should passed after failover happens.
        do {
            server.sayHello(TEST_PARMS);
        }while(SDMocker.getSD().invokeCounts.getAllActivate.get() < 1);
    }

    /**
     * Scenario:
     *    1. 1 instance is registered to SD as passing, but it's actually down.
     *    2. client try to access the service.
     * Expected behavior:
     *    - failover is triggered.
     *    - the access failed and ClientWebApplicationException happens
     */
    @Test
    public void testFailoverButOnlyOneServiceInstance(){
        FakeServer server = TestServices.getServiceClient();
        SERVICE.register(AutoPorts.getNextPort(), true);

        // Call server, failover will happens here
        // Service access failed.
        SDMocker.getSD().invokeCounts.clear();
        try{
            server.sayHello(TEST_PARMS);
            Assert.fail();
        }catch (Exception e){
            Assert.assertEquals(1, SDMocker.getSD().invokeCounts.getAllActivate.get()); // Failover triggered.
        }
    }
}
