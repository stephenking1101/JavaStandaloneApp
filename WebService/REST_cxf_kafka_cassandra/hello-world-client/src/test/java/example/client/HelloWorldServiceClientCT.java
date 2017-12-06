package example.client;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import example.client.api.HelloWorldServiceClient;
import example.client.delegate.HelloWorldServiceDelegate;
import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.foundation.servicediscovery.support.test.SDMocker;
import example.service.api.HelloWorldService;
import example.service.payload.HelloWorld;

import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HelloWorldServiceClientCT {
    private static Logger logger = LoggerFactory.getLogger(HelloWorldServiceClientCT.class);

    private static final SDMocker.MockedService SERVICE = SDMocker.Test_Service;
    private static String uid = "uid-" + HelloWorldServiceClientCT.class.getSimpleName();
    private static final String DEFAULT_CLIENT_ID = "helloworld-client-ct";
    private static HelloWorldServiceClient client;
    private static HelloWorldService helloWorldService;
    private static ConfigurationService configurationService;
    private static volatile int times = 0;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "ct");
        System.setProperty("dp.name", DEFAULT_CLIENT_ID);
        SERVICE.registerOnce();

        mockConfigurationService();
        mockHelloWorldService();
        client = ServiceDiscoveryFactory.getServiceDiscovery().discover(HelloWorldServiceClient.class, null, null);
    }

    public static void mockConfigurationService() {
        ServiceDiscoveryFactory.getMocks().put(ConfigurationService.class, Mockito.mock(ConfigurationService.class));
        configurationService = ServiceDiscoveryFactory.getServiceDiscovery()
                .discover(ConfigurationService.class, null, null);
    }

    private static void mockHelloWorldService() {
        helloWorldService = Mockito.mock(HelloWorldService.class);

        // create and start HelloWorld Service
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setServiceBeanObjects(new HelloWorldServiceDelegate(helloWorldService));

        List<Object> providers = new ArrayList<Object>();
        providers.add(new JacksonJaxbJsonProvider());
        sf.setProviders(providers);
        sf.setAddress(SERVICE.getActualUrl());
        sf.create();
    }

    @Test
    public void testClient() {
        System.out.println("client=" + client);
    }

    @Test
    public void testHelloWorldServiceSayHello() {
        when(configurationService.getBoolean(eq(TestConstants.HELLOWORLD_CLIENT_ENABLED), anyBoolean())).thenReturn(true);

        HelloWorld helloWorld = new HelloWorld();
        helloWorld = new HelloWorld();
    	helloWorld.setUserName(uid);
        helloWorld.setTimestamp(System.currentTimeMillis());

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");

        client.getHelloWorldService().sayHello(helloWorld);
        times++;
        verify(helloWorldService, times(times)).sayHello(any(HelloWorld.class));
    }

    @Test
    public void testHelloWorldServiceNotInvolved() {
        when(configurationService.getBoolean(eq(TestConstants.HELLOWORLD_CLIENT_ENABLED), anyBoolean())).thenReturn(false);

        HelloWorld helloWorld = new HelloWorld();

        client.getHelloWorldService().sayHello(helloWorld);
        verify(helloWorldService, times(times)).sayHello(any(HelloWorld.class));
    }
}
