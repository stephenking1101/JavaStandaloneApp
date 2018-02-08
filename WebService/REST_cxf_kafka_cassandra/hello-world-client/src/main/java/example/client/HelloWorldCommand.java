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

/*流程说明:

1:每次调用创建一个新的HystrixCommand,把依赖调用封装在run()方法中.

2:执行execute()/queue做同步或异步调用.

3:判断熔断器(circuit-breaker)是否打开,如果打开跳到步骤8,进行降级策略,如果关闭进入步骤.

4:判断线程池/队列/信号量是否跑满，如果跑满进入降级步骤8,否则继续后续步骤.

5:调用HystrixCommand的run方法.运行依赖逻辑

5a:依赖逻辑调用超时,进入步骤8.

6:判断逻辑是否调用成功

6a:返回成功调用结果

6b:调用出错，进入步骤8.

7:计算熔断器状态,所有的运行状态(成功, 失败, 拒绝,超时)上报给熔断器，用于统计从而判断熔断器状态.

8:getFallback()降级逻辑.

  以下四种情况将触发getFallback调用：

 (1):run()方法抛出非HystrixBadRequestException异常。

 (2):run()方法调用超时

 (3):熔断器开启拦截调用

 (4):线程池/队列/信号量是否跑满

8a:没有实现getFallback的Command将直接抛出异常

8b:fallback降级逻辑调用成功直接返回

8c:降级逻辑调用失败抛出异常

9:返回执行成功结果*/

/*Metrics在统计各种状态时，时运用滑动窗口思想进行统计的，在一个滑动窗口时间中又划分了若干个Bucket（滑动窗口时间与Bucket成整数倍关系），滑动窗口的移动是以Bucket为单位进行滑动的。 
如：HealthCounts 记录的是一个Buckets的监控状态，Buckets为一个滑动窗口的一小部分，如果一个滑动窗口时间为 t ,Bucket数量为 n，那么每t/n秒将新建一个HealthCounts对象*/
public class HelloWorldCommand extends HystrixCommand<Object> {

	private static final int EXECUTION_TIMEOUT_MS_DEFAULT = 1000;
    private static final int CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE_DEFAULT = 10;
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
                //HystrixCommandKey工厂定义依赖名称, 每个CommandKey代表一个依赖抽象,相同的依赖要使用相同的CommandKey名称。依赖隔离的根本就是对相同CommandKey的依赖做隔离   
                .andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld"))
                //当对同一业务依赖做隔离时使用CommandGroup做区分,但是对同一依赖的不同远程调用如(一个是redis 一个是http),可以使用HystrixThreadPoolKey做隔离区分.
                //最然在业务上都是相同的组，但是需要在资源上做隔离时，可以使用HystrixThreadPoolKey区分.
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ExamplePool"))
                //配置线程池大小,默认值10个. 
                //建议值:请求高峰时99.5%的平均响应时间 + 向上预留一些即可 
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(THREAD_POOL_DEFAULT_SIZE))
                .andCommandPropertiesDefaults(
                		//默认: 错误超过50%且10秒内超过20个请求进行中断拦截
                        HystrixCommandProperties.Setter()
                                //统计滚动的时间窗口,默认:5000毫秒circuitBreakerSleepWindowInMilliseconds  
                                .withMetricsRollingStatisticalWindowInMilliseconds(ROLLING_WINDOW_MS_DEFAULT)
                                //是否启用熔断器,默认true. 启动
                                .withCircuitBreakerEnabled(CIRCUIT_BREAKER_ENABLED_DEFAULT)
                                //熔断器默认工作时间,默认:5秒.熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
                                .withCircuitBreakerSleepWindowInMilliseconds(CIRCUIT_BREAKER_SLEEP_WINDOW_MS_DEFAULT)
                                //熔断器在整个统计时间内是否开启的阀值，默认20次。也就是10秒钟 (根据这个配置ROLLING_WINDOW_MS_DEFAULT) 内至少请求20次，熔断器才发挥起作用
                                .withCircuitBreakerRequestVolumeThreshold(CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD_DEFAULT)
                                //默认:50%。当出错率超过50%后熔断器启动. 
                                .withCircuitBreakerErrorThresholdPercentage(CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE_DEFAULT)
                                //Specifies the timeout value in milliseconds after which the caller observes a timeout and walks away from the command execution. 
                                .withExecutionTimeoutInMilliseconds(EXECUTION_TIMEOUT_MS_DEFAULT)));  
		
		this.helloWorld = helloWorld;
	}

	@Override
	protected Object run() throws Exception {
		logger.debug("Call service in HelloWorldCommand");
		client.getHelloWorldService().sayHello(helloWorld);
		logger.debug("Finish call service in HelloWorldCommand");
		return null;
	}
	
	//使用Fallback() 提供降级策略, 所有从run()方法抛出的异常都算作失败，并触发降级getFallback()和断路器逻辑
	@Override
    protected Object getFallback() {
        logger.warn("Say hello failed, so do nothing");
        logger.debug("Is circuit breaker opened : {}.", this.isCircuitBreakerOpen());
        logger.error("The exception caused by: ", this.getExceptionFromThrowable(getExecutionException()));
        return null;
    }
}
