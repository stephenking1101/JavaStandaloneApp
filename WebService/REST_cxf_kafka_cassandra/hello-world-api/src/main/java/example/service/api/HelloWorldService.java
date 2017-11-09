package example.service.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import example.service.payload.HelloWorld;

@Path("/v1/helloworld")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface HelloWorldService {

	@POST
	void sayHello(HelloWorld helloWorld);
}
