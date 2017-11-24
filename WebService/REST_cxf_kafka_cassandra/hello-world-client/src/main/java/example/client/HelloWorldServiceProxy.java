package example.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import example.configurationservice.ConfigurationService;
import example.foundation.servicediscovery.ServiceDiscoveryFactory;

public class HelloWorldServiceProxy implements InvocationHandler {

    private Object helloWorldService;

    private ConfigurationService configurationService = ServiceDiscoveryFactory.getServiceDiscovery().discover(ConfigurationService.class, null, null);

    private HelloWorldServiceProxy() {
    }

    public HelloWorldServiceProxy(Object helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    //工厂方法，实例化一个动态代理，代理helloWorldService
    public static Object getHelloWorldServiceProxy(Object helloWorldService) {
        Class cls = helloWorldService.getClass();
        return Proxy.newProxyInstance(cls.getClassLoader(),
                cls.getInterfaces(), new HelloWorldServiceProxy(helloWorldService));
    }

    private boolean isHelloWorldClientEnabled() {
        return configurationService.getBoolean("helloworld.client.enabled", false);
    }

    //调用helloWorldService方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;

        if(isHelloWorldClientEnabled()){
            result = method.invoke(helloWorldService, args);
        }

        return result;
    }
}
