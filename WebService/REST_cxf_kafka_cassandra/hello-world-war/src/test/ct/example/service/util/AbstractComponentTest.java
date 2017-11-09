package example.service.util;

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import com.jayway.restassured.RestAssured;

import example.test.cf.EmbeddedCassandraHelper;
import example.test.cf.JettyFactory;


public class AbstractComponentTest {

    private static Logger logger = LoggerFactory.getLogger(AbstractComponentTest.class);
    private static Server jettyServer;

    private static final String BASE_HOST = "http://localhost";
    private static final int SERVICE_PORT = 27220;
    private static final String SERVICE_CONTEXT = "/helloworld";
    private static final String SERVICE_BASE_URL = BASE_HOST + HelloWorldTestConstants.COLON_DELIMITER + SERVICE_PORT + SERVICE_CONTEXT;

    // Req Params
    protected final String contentType = "application/json";
    protected final String accept = "application/json";
    protected final String language = "es,en,en_US";
    protected final String sourceIP = "127.0.0.1";

    @BeforeClass
    public static void doBeforeClass() throws Exception {
        EmbeddedCassandraHelper.startEmbeddedCassandraAndLoadData();
        startJettyOnce();
        initRestAssured();
    }

    @AfterClass
    public static void teardown() {
    }

    private static void initRestAssured() {
        RestAssured.baseURI = BASE_HOST;
        RestAssured.port = SERVICE_PORT;
        RestAssured.basePath = SERVICE_CONTEXT;
    }

    private static void startJettyOnce() throws Exception {
        if (jettyServer == null) {
            jettyServer = startJetty(SERVICE_BASE_URL);
        }

    }

    protected static Server startJetty(String url) throws Exception {
        if (new URL(url).getHost().equals("localhost")) {

            System.setProperty("spring.profiles.active", "ct");

            Server server = JettyFactory.createServer(new URL(url).getPort(), new URL(url).getPath(), "src/main/webapp");

            server.start();
            logger.info("Jetty Server started. URL: " + url);
            return server;
        }

        logger.info("Base URI is not localhost: " + url);
        return null;
    }

    protected static void timeoutEquals(Object expect, TimeoutAssert.TimeOutFunction actualFunction) throws InterruptedException {
        TimeoutAssert.timeoutEquals(2, expect, actualFunction);
    }

}
