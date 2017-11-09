package example.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import example.provider.HelloWorldProducer;
import example.service.payload.HelloWorld;

public class HelloWorldManager {

	private static Logger logger = LoggerFactory.getLogger(HelloWorldManager.class);
	
    private HelloWorldProducer helloWorldProducer;

    public void processSayHello(HelloWorld helloWorld){
        logger.debug("process receive {}", helloWorld);
        helloWorld.setTimestamp(System.currentTimeMillis());
        helloWorldProducer.send(helloWorld);
    }

    @Autowired
	public void setHelloWorldProducer(HelloWorldProducer helloWorldProducer) {
		this.helloWorldProducer = helloWorldProducer;
	}
}
