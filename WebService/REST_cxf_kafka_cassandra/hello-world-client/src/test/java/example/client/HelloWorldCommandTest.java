package example.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;

import example.client.api.HelloWorldServiceClient;
import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.service.api.HelloWorldService;
import example.service.payload.HelloWorld;

public class HelloWorldCommandTest {
	
	private static Logger logger = LoggerFactory.getLogger(HelloWorldCommandTest.class);
	
	@Mock
	private HelloWorldService helloWorldService;
	
	@Mock
    private ConfigurationService configurationService;
	
	@Mock
	private static HelloWorldServiceClient client;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
        ServiceDiscoveryFactory.getMocks().put(ConfigurationService.class, configurationService);
        ServiceDiscoveryFactory.getMocks().put(HelloWorldServiceClient.class, client);
        Mockito.when(client.getHelloWorldService()).thenReturn(helloWorldService);
	}
    
	@After
    public void resetMock() {
        Mockito.reset(configurationService);
	}
	
	@Test
    public void testCircuitBreaker() {
    	HelloWorld helloWorld = new HelloWorld();
    	
    	//-------test circuit breaker is opened when reached request volume threshold and error threshold percentage----
        for (int i = 1; i <= 6; i++) {
            if (i == 3) {
            	mockServiceExceptionResponse();
            } 

            if (i == 4){
            	mockServiceNormalResponse();
            }
            
            HelloWorldCommand sender = new HelloWorldCommand(helloWorld);
            HystrixCommandMetrics metrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey("HelloWorld"));
            sender.execute();
            
            if (i < 3) { // the response is returned from run method of sender before the fourth time
                assertFalse(sender.isCircuitBreakerOpen());
                assertFalse(sender.isResponseFromFallback());
            } else if (i == 3) { // the response is returned from FallBack method of sender and not trigger the circuit breaker
                assertFalse(sender.isCircuitBreakerOpen());
                assertTrue(sender.isResponseFromFallback());
            } else if (i > 3) { // circuit breaker is opened from the fourth time(more than 3 request and 10% error percentage in 600s)
                assertTrue(sender.isCircuitBreakerOpen());
                assertTrue(sender.isResponseFromFallback());
            }
            
            logger.debug("Error conut: " + metrics.getHealthCounts().getErrorCount());
            logger.debug("Error percentage: " +metrics.getHealthCounts().getErrorPercentage());
            logger.debug("Total requests sent: " + metrics.getHealthCounts().getTotalRequests());
            logger.debug("The mean (average) execution time (in milliseconds): " + metrics.getExecutionTimeMean());
            
            if (i == 3) {
                sleep(500); // after the third time, the circuit breaker is open, need wait at least 500 ms (defined in HystrixCommandProperties default_metricsHealthSnapshotIntervalInMilliseconds) to sync up the healthcounts
            }
        }
        
        verify(helloWorldService, times(3)).sayHello(any(HelloWorld.class));
    	
        // ----------- test circuit breaker turn to close after sleep window ms ----------------------
        sleep(1000); // exceed sleep window ms
        
        HelloWorldCommand sender = new HelloWorldCommand(helloWorld);
        HystrixCommandMetrics metrics = HystrixCommandMetrics.getInstance(HystrixCommandKey.Factory.asKey("HelloWorld"));
        sender.execute();
        sleep(500);
        
        assertFalse(sender.isCircuitBreakerOpen());
        assertFalse(sender.isResponseFromFallback());
        logger.debug("Error conut: " + metrics.getHealthCounts().getErrorCount());
        logger.debug("Error percentage: " +metrics.getHealthCounts().getErrorPercentage());
        logger.debug("Total requests sent: " + metrics.getHealthCounts().getTotalRequests());
        logger.debug("The mean (average) execution time (in milliseconds): " + metrics.getExecutionTimeMean());
        
        
        /*HystrixCircuitBreaker.Factory.getInstance(HystrixCommandKey.Factory.asKey("HelloWorld")).markSuccess();
        // shutdown all thread pools; waiting a little time for shutdown
        Hystrix.reset(10, TimeUnit.SECONDS);
        
	    // shutdown configuration listeners that might have been activated by
	    // Archaius
	    if (ConfigurationManager.getConfigInstance() instanceof DynamicConfiguration) {
	         ((DynamicConfiguration) ConfigurationManager.getConfigInstance())
	                 .stopLoading();
	    } else if (ConfigurationManager.getConfigInstance() instanceof ConcurrentCompositeConfiguration) {
	         ConcurrentCompositeConfiguration config =
	                 ((ConcurrentCompositeConfiguration) ConfigurationManager
	                         .getConfigInstance());
	         for (AbstractConfiguration innerConfig : config.getConfigurations()) {
	             if (innerConfig instanceof DynamicConfiguration) {
	                 ((DynamicConfiguration) innerConfig).stopLoading();
	             }
	         }
	    }*/
    }
    
    private void mockServiceExceptionResponse(){
    	Mockito.doThrow(new RuntimeException("Test sayHello")).when(helloWorldService).sayHello(Mockito.any(HelloWorld.class));
    }
    
    private void mockServiceNormalResponse(){
    	Mockito.doNothing().when(helloWorldService).sayHello(Mockito.any(HelloWorld.class));
    }
    
    private void sleep(int millionSeconds) {
        try {
            Thread.sleep(millionSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
