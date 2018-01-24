package example.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;

import example.constants.HelloWorldConstants;
import example.dao.HelloWorldDao;
import example.service.payload.HelloWorld;

public class HelloWorldConsumer {
	private static Logger logger = LoggerFactory.getLogger(HelloWorldConsumer.class);

    private HelloWorldDao helloWorldDao;

    //in order to initialize the properties and set it to manual commit
    @Autowired
    public ContainerProperties containerProperties;

    @KafkaListener(id="save-to-cass", topics=HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD)
    public void save(HelloWorld helloWorld, Acknowledgment ack){
    	try {
	    	//containerProperties.setAckMode(AckMode.MANUAL);
	        logger.debug("save to cassandra {}", helloWorld);
	        helloWorldDao.create(helloWorld);
	        
	        //commit the offset
	        ack.acknowledge();
    	} catch (Throwable e){
            logger.error("Error when saving the message", e);
        }
    }

    @Autowired
	public void setHelloWorldDao(HelloWorldDao helloWorldDao) {
		this.helloWorldDao = helloWorldDao;
	}

}
