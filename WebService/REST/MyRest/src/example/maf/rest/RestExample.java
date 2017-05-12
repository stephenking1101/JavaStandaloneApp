package example.maf.rest;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

//Assign URI 
@Path("/helloworld")
public class RestExample {
	// Handle HTTP GET request
	@GET
	// response format is "text/plain"
	@Path("/{param}")
	@Produces("text/plain")
	public String getClichedMessage(@PathParam("param") String name) {
		return name + " Say : Hello World.";
	}

	public static void main(String[] args) throws IOException {
		// create RESTful WebService
		HttpServer server = HttpServerFactory.create("http://localhost:9999/");
		// start service , a new thread will be created
		server.start();
		// output message to console
		System.out.println("RESTful WebService started");
		System.out.println("Server url: http://localhost:9999/helloworld/yourname");
	}
}
