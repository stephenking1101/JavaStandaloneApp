package example.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.constants.HelloWorldConstants;
import example.service.payload.HelloWorld;

public class OriginalKafkaConsumer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(OriginalKafkaConsumer.class);

	private volatile boolean running = false;
	private Map<String, Object> consumerConfigs;
    private KafkaConsumer<String, HelloWorld> kafkaConsumer;
    
    
	public OriginalKafkaConsumer(Map<String, Object> consumerConfigs) {
	    consumerConfigs.put("group.id", "test");
	    
		this.consumerConfigs = consumerConfigs;
		this.kafkaConsumer= new KafkaConsumer<String, HelloWorld>(consumerConfigs);
		this.kafkaConsumer.subscribe(Arrays.asList(HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD));
	}
	
	public void init(){
        synchronized (this) {
            if (!isRunning()) {
                setRunning(true);
                Thread thread = new Thread(this);

                thread.setDaemon(true);
                thread.start();
            }
        }
    }

	@Override
	public void run() {
		while (isRunning()){
			ConsumerRecords<String, HelloWorld> records = kafkaConsumer.poll(100);
			
			for (ConsumerRecord<String, HelloWorld> record : records){
				try {
					logger.debug("Retrieved message {}", record.value());
		            System.out.println(record.value());
		            
		            //consider as consumed until it is completed processing
		            kafkaConsumer.commitSync();
		        } catch (Throwable e) {
		            logger.error("Error when saving the user activity logs", e);
		            //client is the one that keeps the consumed offsets. The commit sends the offsets to the server, but it has no effect on next poll from that client
			        //Why is then the offset sent to the server? For next rebalance. So the only situation server uses the committed offsets is when some client dies/disconnects - then the partitions are rebalanced and with this rebalances the clients get the offsets from the server.
			        //So if you don't commit offset and then call poll(), you cannot expect that the message will be read again. To this there would have to be a possibility to rollback the offset in the client by calling KafkaConsumer.seek to the offset of failed message
		            //Approach 1:
		            kafkaConsumer.seek(new TopicPartition(record.topic(), record.partition()), record.offset());
		            
		            //Approach 2:
		            /*Set<TopicPartition> partitions = kafkaConsumer.assignment();
		            for(TopicPartition p : partitions){
		                OffsetAndMetadata offset = kafkaConsumer.committed(p);
		                if(offset != null){
		                    kafkaConsumer.seek(p, offset.offset());
		                    continue;
		                }

		                kafkaConsumer.seekToBeginning(Collections.singleton((TopicPartition) p));
		            }*/
		        }
			}
		}
	}

	public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
