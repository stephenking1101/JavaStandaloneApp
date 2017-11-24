package example.client.delegate;

import example.service.api.HelloWorldService;
import example.service.payload.HelloWorld;

public class HelloWorldServiceDelegate implements HelloWorldService {

    private HelloWorldService implement;

    public HelloWorldServiceDelegate(HelloWorldService implement) {
        this.implement = implement;
    }

    @Override
    public void sayHello(HelloWorld helloWorld) {
        implement.sayHello(helloWorld);
    }
}
