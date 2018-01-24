package example.service.impl;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;
import static org.springframework.kafka.test.assertj.KafkaConditions.value;

import javax.ws.rs.core.Response;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import example.constants.HelloWorldConstants;
import example.service.payload.HelloWorld;
import example.service.util.AbstractComponentTest;
import example.service.util.EmbeddedKafkaHelper;
import example.service.util.HelloWorldTestConstants;
import example.test.common.EmbeddedCassandraHelper;

public class HelloWorldServiceCT extends AbstractComponentTest {
    private HelloWorld helloWorld;

    private static String uid = "uid-" + HelloWorldServiceCT.class.getSimpleName();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static EmbeddedKafkaHelper kafkaHelper;

    @BeforeClass
    public static void setup() throws Exception {
        kafkaHelper = new EmbeddedKafkaHelper(HelloWorldConstants.KAFKA_TOPIC_HELLO_WORLD);
    }

    @Before
    public void before() {
        initRequestParams();
        kafkaHelper.clear();
    }

    @AfterClass
    public static void after() {
        kafkaHelper.clear();
        //kafkaHelper.after();
    }

    private void initRequestParams() {
    	helloWorld = new HelloWorld();
    	helloWorld.setUserName(uid);
        helloWorld.setTimestamp(System.currentTimeMillis());

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");
    }

    @Test
    public void testSavePositiveFlow() throws Exception {
        String countCql = "select count(1) from hello_world where user_name='" + uid + "'";
        Long countBefore = EmbeddedCassandraHelper.queryForCount(countCql);

        RestAssured.given().log().all().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(helloWorld))
                .when().post(HelloWorldTestConstants.URL_HELLOWORLD)
                .then().log().all().assertThat()
                .statusCode(Response.Status.OK.getStatusCode());

        timeoutEquals(countBefore + 1, () -> EmbeddedCassandraHelper.queryForCount(countCql));
    }

    @Test
    public void testListenerFlow() throws Exception {
        String countCql = "select count(1) from hello_world where user_name='" + uid + "'";
        Long countBefore = EmbeddedCassandraHelper.queryForCount(countCql);

        kafkaHelper.send(uid, helloWorld);

        timeoutEquals(countBefore + 1, () -> EmbeddedCassandraHelper.queryForCount(countCql));
    }

    @Test
    public void testProducerFlow() throws Exception {
        RestAssured.given().log().all().contentType(ContentType.JSON).body(objectMapper.writeValueAsString(helloWorld))
                .when().post(HelloWorldTestConstants.URL_HELLOWORLD)
                .then().log().all().assertThat()
                .statusCode(Response.Status.OK.getStatusCode());

        ConsumerRecord<String, String> received = (ConsumerRecord<String, String>) kafkaHelper.receive();
        assertEquals(uid, received.key());
    }
    
    @Test
    public void testEmbeddedKafka() throws Exception{
        kafkaHelper.send("key", helloWorld);
        //use objectMapper to serialize any Java value as a String
        assertThat((ConsumerRecord<String, String>) kafkaHelper.receive()).has(value(objectMapper.writeValueAsString(helloWorld)));
        
        kafkaHelper.send("2", helloWorld);
        ConsumerRecord<String, String> received = (ConsumerRecord<String, String>) kafkaHelper.receive();
        assertThat(received).has(key("2"));
        assertThat(received).has(value(objectMapper.writeValueAsString(helloWorld)));
    }
}
