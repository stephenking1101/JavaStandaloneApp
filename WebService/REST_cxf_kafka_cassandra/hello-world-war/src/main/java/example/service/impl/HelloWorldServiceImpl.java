package example.service.impl;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletResponse;

import example.business.HelloWorldManager;
import example.service.api.HelloWorldService;
import example.service.payload.HelloWorld;

public class HelloWorldServiceImpl implements HelloWorldService{
	
	private static Logger logger = LoggerFactory.getLogger(HelloWorldServiceImpl.class);

	private HelloWorldManager helloWorldManager;
	
	@Context
    private MessageContext messageContext;
	

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }
	
	@Override
	public void sayHello(HelloWorld helloWorld) {
		logger.debug("sayHello {}", helloWorld);
		helloWorldManager.processSayHello(helloWorld);
		
		HttpServletResponse response = messageContext.getHttpServletResponse();
        response.setStatus(Response.Status.OK.getStatusCode());
	}

	@Autowired
	public void setHelloWorldManager(HelloWorldManager helloWorldManager) {
		this.helloWorldManager = helloWorldManager;
	}

}
