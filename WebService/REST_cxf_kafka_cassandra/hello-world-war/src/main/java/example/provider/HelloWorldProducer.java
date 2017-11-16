package example.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import example.constants.HelloWorldConstants;
import example.service.payload.HelloWorld;

public class HelloWorldProducer {

	private static Logger logger = LoggerFactory.getLogger(HelloWorldProducer.class);
	
	private KafkaTemplate<String, HelloWorld> kafkaTemplate;

	public void send(HelloWorld helloWorld){
        logger.debug("send to kafka{}", helloWorld);
        kafkaTemplate.send(HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD, helloWorld.getUserName(), helloWorld);
    }
	
	@Autowired
    public void setKafkaTemplate(KafkaTemplate<String, HelloWorld> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
}
