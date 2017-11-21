package example.client;

import example.client.api.HelloWorldServiceClient;
import example.service.api.HelloWorldService;

public class HelloWorldServiceClientImpl implements HelloWorldServiceClient {

    private HelloWorldService helloWorldService;

    @Override
    public HelloWorldService getHelloWorldService() {
        return helloWorldService;
    }

    public void setHelloWorldService(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }
}
