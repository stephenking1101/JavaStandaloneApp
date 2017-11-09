package example.service.util;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
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

    private static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true,
    		HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD);

    static {
        try {
            embeddedKafka.before();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBrokersAsString(){
        return embeddedKafka.getBrokersAsString();
    }

    private KafkaTemplate<String, Object> template;
    private final BlockingQueue<ConsumerRecord<String, Object>> records = new LinkedBlockingQueue<>();

    public EmbeddedKafkaHelper(String topic) throws Exception {
        initTemplate(topic);
        startConsumer(topic);
    }

    public void send(String key, Object value) {
        template.sendDefault(key, value);
    }

    public void initTemplate(String topic) {
        Map<String, Object> senderProps = KafkaTestUtils.senderProps(embeddedKafka.getBrokersAsString());
        senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(senderProps);
        template = new KafkaTemplate<>(pf);
        template.setDefaultTopic(topic);
    }

    public void startConsumer(String topic) throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group-" +
                EmbeddedKafkaHelper.class.getSimpleName(), "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, Object> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(topic);
        KafkaMessageListenerContainer<String, Object> container =
                new KafkaMessageListenerContainer<>(cf, containerProperties);
        container.setupMessageListener((MessageListener<String, Object>) record -> records.add(record));
        container.setBeanName("templateTests");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    public void clear(){
        records.clear();
    }

    public Object receive() throws InterruptedException {
        return records.poll(2, TimeUnit.SECONDS);
    }

}
