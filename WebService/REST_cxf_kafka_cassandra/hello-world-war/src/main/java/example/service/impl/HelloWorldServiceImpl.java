package example.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import example.business.HelloWorldManager;
import example.service.api.HelloWorldService;
import example.service.payload.HelloWorld;

public class HelloWorldServiceImpl implements HelloWorldService{
	
	private static Logger logger = LoggerFactory.getLogger(HelloWorldServiceImplTest.class);

	private HelloWorldManager helloWorldManager;
	
	@Override
	public void sayHello(HelloWorld helloWorld) {
		logger.debug("sayHello {}", helloWorld);
		helloWorldManager.processSayHello(helloWorld);
	}

	@Autowired
	public void setHelloWorldManager(HelloWorldManager helloWorldManager) {
		this.helloWorldManager = helloWorldManager;
	}

}
