package example.business;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import example.provider.HelloWorldProducer;
import example.service.payload.HelloWorld;

public class HelloWorldManagerTest {
	@InjectMocks
    private HelloWorldManager helloWorldManager;
    @Mock
    private HelloWorldProducer helloWorldProducer;

    @Mock
    private KafkaTemplate<String, HelloWorld> kafkaTemplate;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSendToKafka() {
    	String uName = "get_u_1002";
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.setUserName(uName);
        helloWorld.setTimestamp(System.currentTimeMillis());

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");
        
        helloWorldManager.processSayHello(helloWorld);
        verify(helloWorldProducer).send(any(HelloWorld.class));
    }
}
