package example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import example.client.api.HelloWorldServiceClient;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;
import example.service.payload.HelloWorld;

public class HelloWorldCommand extends HystrixCommand<Object> {

	private static final int EXECUTION_TIMEOUT_MS_DEFAULT = 1000;
    private static final int CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE_DEFAULT = 80;
    private static final int CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD_DEFAULT = 3;
    private static final int CIRCUIT_BREAKER_SLEEP_WINDOW_MS_DEFAULT = 500;
    private static final boolean CIRCUIT_BREAKER_ENABLED_DEFAULT = true;
    private static final int THREAD_POOL_DEFAULT_SIZE = 5;
    private static final int ROLLING_WINDOW_MS_DEFAULT = 10 * 60 * 1000;
	private HelloWorld helloWorld;
	private static Logger logger = LoggerFactory.getLogger(HelloWorldCommand.class);
	private HelloWorldServiceClient client = ServiceDiscoveryFactory.getServiceDiscovery().discover(HelloWorldServiceClient.class, null, null);
	
	public HelloWorldCommand(HelloWorld helloWorld){
		//CommandGroup是每个命令最少配置的必选参数，在不指定ThreadPoolKey的情况下，字面值用于对不同依赖的线程池/信号区分
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))  
                /* HystrixCommandKey工厂定义依赖名称, 每个CommandKey代表一个依赖抽象,相同的依赖要使用相同的CommandKey名称。依赖隔离的根本就是对相同CommandKey的依赖做隔离 */  
                .andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ExamplePool"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(THREAD_POOL_DEFAULT_SIZE))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                                .withMetricsRollingStatisticalWindowInMilliseconds(ROLLING_WINDOW_MS_DEFAULT)
                                //是否启用熔断器,默认true. 启动
                                .withCircuitBreakerEnabled(CIRCUIT_BREAKER_ENABLED_DEFAULT)
                                //熔断器默认工作时间,默认:5秒.熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
                                .withCircuitBreakerSleepWindowInMilliseconds(CIRCUIT_BREAKER_SLEEP_WINDOW_MS_DEFAULT)
                                // 熔断器在整个统计时间内是否开启的阀值，默认20秒。也就是10秒钟内至少请求20次，熔断器才发挥起作用
                                .withCircuitBreakerRequestVolumeThreshold(CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD_DEFAULT)
                                //默认:50%。当出错率超过50%后熔断器启动. 
                                .withCircuitBreakerErrorThresholdPercentage(CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE_DEFAULT)
                                .withExecutionTimeoutInMilliseconds(EXECUTION_TIMEOUT_MS_DEFAULT)));  
		
		this.helloWorld = helloWorld;
	}

	@Override
	protected Object run() throws Exception {
		client.getHelloWorldService().sayHello(helloWorld);
		return null;
	}
	
	//使用Fallback() 提供降级策略, 所有从run()方法抛出的异常都算作失败，并触发降级getFallback()和断路器逻辑
	@Override
    protected Object getFallback() {
        logger.warn("Say hello failed, so do nothing");
        return null;
    }
}
