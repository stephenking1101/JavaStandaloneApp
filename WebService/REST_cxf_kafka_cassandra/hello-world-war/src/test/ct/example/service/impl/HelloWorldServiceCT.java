package example.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasPartition;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.jayway.restassured.RestAssured;

import example.constants.HelloWorldConstants;
import example.service.payload.HelloWorld;
import example.service.util.AbstractComponentTest;
import example.service.util.EmbeddedKafkaHelper;
import example.service.util.HelloWorldTestConstants;
import example.test.cf.EmbeddedCassandraHelper;

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
    }

    @Before
    public void after() {
        kafkaHelper.clear();
    }

    private void initRequestParams() {
    	helloWorld = new HelloWorld();
    	helloWorld.setUserName("abc");
        helloWorld.setTimestamp(System.currentTimeMillis());

        helloWorld.setExtension("v_int", 123);
        helloWorld.setExtension("v_bool", true);
        helloWorld.setExtension("v_str", "");
    }

    @Test
    public void testSavePositiveFlow() throws Exception {
        String countCql = "select count(1) from hello_world where user_name='" + uid + "'";
        Long countBefore = EmbeddedCassandraHelper.queryForCount(countCql);

        RestAssured.given().log().all().body(objectMapper.writeValueAsString(helloWorld))
                .when().post(HelloWorldTestConstants.URL_HELLOWORLD)
                .then().log().all().assertThat()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

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
        RestAssured.given().log().all().body(objectMapper.writeValueAsString(helloWorld))
                .when().post(HelloWorldTestConstants.URL_HELLOWORLD)
                .then().log().all().assertThat()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        ConsumerRecord<String, String> received = (ConsumerRecord<String, String>) kafkaHelper.receive();
        assertEquals(uid, received.key());
    }
}
