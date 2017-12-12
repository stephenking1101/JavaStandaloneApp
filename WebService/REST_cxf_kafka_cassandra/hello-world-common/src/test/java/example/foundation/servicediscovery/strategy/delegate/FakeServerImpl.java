package example.foundation.servicediscovery.strategy.delegate;

public class FakeServerImpl implements FakeServer{
    public String sayHello(String name){
        return "Hello " + name;
    }
    public String toLower(String name){
        return name.toLowerCase();
    }
}
