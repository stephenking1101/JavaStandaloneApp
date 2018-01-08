package example.service.impl;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.jayway.restassured.RestAssured;

import example.service.util.AbstractComponentTest;

public class HealthCheckCT extends AbstractComponentTest {
	@Test
    public void testGetHealthCheck() throws Exception{
        RestAssured
                .when().get("/healthcheck")
                .then().log().all()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}
