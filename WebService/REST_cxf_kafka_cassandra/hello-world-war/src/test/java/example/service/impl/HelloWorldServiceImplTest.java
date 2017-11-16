package example.service.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import example.business.HelloWorldManager;
import example.service.payload.HelloWorld;

public class HelloWorldServiceImplTest {
	@InjectMocks
    private HelloWorldServiceImpl helloWorldService;
    @Mock
    private HelloWorldManager helloWorldManager;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateHelloWorldSuccess() {
    	String uName = "get_u_1002";
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.setUserName(uName);
        helloWorld.setTimestamp(System.currentTimeMillis());

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");
        
        helloWorldManager.processSayHello(helloWorld);
        verify(helloWorldManager).processSayHello(eq(helloWorld));
    }
}
