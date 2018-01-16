package example.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;

public class ThrowableMapper  implements ExceptionMapper<Throwable> {
	
	private Logger logger;

	@Override
	public Response toResponse(Throwable exception) {
		logger.error(exception.getMessage(), exception);

        AACommonError error = new AACommonError("server_error",
                "Internal server error.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(error).build();
	}

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
