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

    public static Object getRbaServiceProxy(Object helloWorldService) {
        Class cls = helloWorldService.getClass();
        return Proxy.newProxyInstance(cls.getClassLoader(),
                cls.getInterfaces(), new HelloWorldServiceProxy(helloWorldService));
    }

    private boolean isRbaClientEnabled() {
        return configurationService.getBoolean("helloworld.client.enabled", false);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;

        if(isRbaClientEnabled()){
            result = method.invoke(helloWorldService, args);
        }

        return result;
    }
}
