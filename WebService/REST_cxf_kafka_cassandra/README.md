# RESTful Service with Kafka and CXF

## Involve the client

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