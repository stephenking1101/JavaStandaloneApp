package example.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import example.constants.HelloWorldConstants;
import example.dao.HelloWorldDao;
import example.service.payload.HelloWorld;

public class HelloWorldConsumer {
	private static Logger logger = LoggerFactory.getLogger(HelloWorldConsumer.class);

    private HelloWorldDao helloWorldDao;

    @KafkaListener(id="save-to-cass", topics=HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD)
    public void save(HelloWorld helloWorld, Acknowledgment ack){
        logger.debug("save to cassandra {}", helloWorld);
        helloWorldDao.create(helloWorld);
        ack.acknowledge();
    }

    @Autowired
	public void setHelloWorldDao(HelloWorldDao helloWorldDao) {
		this.helloWorldDao = helloWorldDao;
	}

}
