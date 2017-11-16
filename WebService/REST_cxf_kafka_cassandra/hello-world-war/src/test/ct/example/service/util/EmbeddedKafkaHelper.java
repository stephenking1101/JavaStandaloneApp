package example.service.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import example.constants.HelloWorldConstants;


public class EmbeddedKafkaHelper {

    private static KafkaEmbedded embeddedKafka;

    static {
        try {
        	embeddedKafka = new KafkaEmbedded(1, true, HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD);
        	
        	Map<String, String> brokerProperties = new HashMap<String, String>();
        	brokerProperties.put("advertised.host.name", "localhost");
        	brokerProperties.put("advertised.port", "9092");
        	brokerProperties.put("auto.create.topics.enable", "false");
        	
        	embeddedKafka.setKafkaPorts(9092);
        	embeddedKafka.brokerProperties(brokerProperties);
            embeddedKafka.before();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBrokersAsString(){
        return embeddedKafka.getBrokersAsString();
    }

    private KafkaTemplate<String, Object> template;
    // create a thread safe queue to store the received message
    private final BlockingQueue<ConsumerRecord<String, Object>> records = new LinkedBlockingQueue<>();

    public EmbeddedKafkaHelper(String topic) throws Exception {
        initTemplate(topic);
        startConsumer(topic);
    }

    public void send(String key, Object value) {
        template.sendDefault(key, value);
    }

    public void initTemplate(String topic) {
    	// set up the Kafka producer properties
        Map<String, Object> senderProps = KafkaTestUtils.senderProps(embeddedKafka.getBrokersAsString());
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // create a Kafka producer factory
        ProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(senderProps);
        // create a Kafka template
        template = new KafkaTemplate<>(pf);
        // set the default topic to send to
        template.setDefaultTopic(topic);
    }

    public void startConsumer(String topic) throws Exception {
    	// set up the Kafka consumer properties
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group-" +
                EmbeddedKafkaHelper.class.getSimpleName(), "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, Object> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        // set the topic that needs to be consumed
        ContainerProperties containerProperties = new ContainerProperties(topic);
        // create a Kafka MessageListenerContainer
        KafkaMessageListenerContainer<String, Object> container =
                new KafkaMessageListenerContainer<>(cf, containerProperties);
        container.setupMessageListener((MessageListener<String, Object>) record -> records.add(record));
        container.setBeanName("templateTests");
        // start the container and underlying message listener
        container.start();
        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    public void clear(){
        records.clear();
    }

    public Object receive() throws InterruptedException {
        return records.poll(10, TimeUnit.SECONDS);
    }

    public void after(){
    	try {
			embeddedKafka.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
