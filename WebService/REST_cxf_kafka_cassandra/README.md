# RESTful Service with Kafka and CXF

## Service - hello-world-war

Contains main logic of the service

Health check path: http://127.0.0.1:8080/hello-world-war/healthcheck is defined in web.xml

## API definition - hello-world-api

Contiains the RESTful API definition for the service

## DB access - hello-world-store-cass

Contains the database access methods

Table definition is in create-table.cql

## Http client project - hello-world-cient

* How to involve the client to access the service

```
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( System.getProperty("java.library.path") );
        
        System.setProperty("HELLO_WORLD_SERVICE", "http://127.0.0.1:8080/hello-world-war");
        HelloWorldServiceClient client = ServiceDiscoveryFactory.getServiceDiscovery().discover(HelloWorldServiceClient.class, null, null);
        HelloWorld helloWorld = new HelloWorld();
        helloWorld = new HelloWorld();
    	helloWorld.setUserName("me");
        helloWorld.setTimestamp(System.currentTimeMillis());

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");

        client.getHelloWorldService().sayHello(helloWorld);
    }
}
```

* How to involve the client with circuit breaker [Hystrix](https://github.com/Netflix/Hystrix/wiki/How-it-Works#CircuitBreaker)

```
HelloWorld helloWorld = new HelloWorld();

        //Asynchronous involve
        //new HelloWorldCommand(helloWorld).queue();
        
        //Synchronous involve
        new HelloWorldCommand(helloWorld).execute();
```

## hello-world-parent contains the dependency definition

## hello-world-ft is for end-to-end function test for the service

## hello-world-docker to is package the service into a docker image

## hello-world-common contains the service discovery logic and DB access utils

service discovery logic:  
1. 按spi标准获得ServiceFactory的实现
2. 通过AbstractSpringServiceFactory初始化spring context
3. 通过ServiceFactory的实现获取bean, 背后其实是是调用applicationContext.getBean（）

META-INF folder is used by ServiceLoader.class

service discovery过程大致如下       
1. 查询System.getProperties(), key="XXX_XXX_SERVICE"  
2. 查询System.getenv() 即环境变量,, key="XXX_XXX_SERVICE"  
3. 查询"/etc/modules/service-disc/config/sd_service_sync.yml”(consul中service同步文件，路径可配）， key="xxx-xxx"  (在sd-backend-localfile-impl project 实现, sd-backend-consul-impl为利用consul服务发现的实现)

service_list.json is the service definition for SDMocker.class

aa_error_config.json contains the error description for the AppCommonException.class

## configuration-local-service contains the configuration management logic to load the configuration properties at runtime

It reads the file from absolutePath='C:\etc\modules...'
