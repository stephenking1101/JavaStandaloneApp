package example.foundation.servicediscovery.cxfclient.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.After;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import example.foundation.servicediscovery.strategy.delegate.FakeServer;
import example.foundation.servicediscovery.strategy.delegate.FakeServerImpl;
import example.foundation.servicediscovery.support.AddressProvider;
import example.foundation.servicediscovery.support.CXFClientHelper;
import example.foundation.servicediscovery.support.test.AutoPorts;
import example.foundation.servicediscovery.support.test.SDMocker;
import example.foundation.servicediscovery.support.util.ServiceHelper;

public class AbstractCXFClientTest {
    protected static final SDMocker.MockedService SERVICE = SDMocker.Test_Service;
    protected static final String TEST_PARMS = "CXFCLIENT_TEST";

    @After
    public void clearService(){
        TestServices.stopAllServices();
        SERVICE.deregister();
    }

    protected static class TestServices {
        private static Map<Integer, Server> services = new ConcurrentHashMap<Integer, Server>();

        static FakeServer getServiceClient(){
            return getClientFactory().create(FakeServer.class);
        }

        static void stopService(int port){
            SERVICE.register(port, false);
            Server server = services.remove(port);
            if (server != null){
                server.destroy();
            }
        }

        static void stopAllServices(){
            for (int port : services.keySet()){
                stopService(port);
            }
            services.clear();
        }

        static int startService(){
            int port = AutoPorts.getNextPort();
            startService(port);
            SERVICE.register(port, true);
            return port;
        }

        static int startService(int port){
            // create start Fake Service
            JAXRSServerFactoryBean serverFactory = new JAXRSServerFactoryBean();
            serverFactory.setServiceBeanObjects(new FakeServerImpl());
            List<Object> providers = new ArrayList<Object>();
            providers.add(new JacksonJaxbJsonProvider());
            serverFactory.setProviders(providers);
            serverFactory.setAddress(SERVICE.getServiceUriFormat().replace(ServiceHelper.SERVICE_NAME_KEY, "localhost:" +port));

            Server server = serverFactory.create();
            services.put(port, server);
            return port;
        }

        private static JAXRSClientFactoryBean getClientFactory(){
            AddressProvider addressProvider = new AddressProvider(SERVICE.getServiceName());

            JAXRSClientFactoryBean clientFactory = CXFClientHelper.getClientFactory(addressProvider);
            clientFactory.setResourceClass(FakeServer.class);
            clientFactory.setServiceClass(FakeServer.class);
            return clientFactory;
        }
    }
}
