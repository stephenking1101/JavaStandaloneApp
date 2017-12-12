package example.foundation.servicediscovery.strategy.delegate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FakeServer {
    @GET
    @Path("/sayhello/{name}")
    String sayHello(@PathParam("name") String name);

    @GET
    @Path("/tolower/{name}")
    String toLower(@PathParam("name") String name);
}