package example.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

@Provider
public class AppCommonExceptionMapper implements ExceptionMapper<AppCommonException> {

    private Logger logger;

    @Override
    public Response toResponse(AppCommonException e) {
        if (e.getHttpStatus() >= Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            logger.error(e.getMessage(), e);
        } else {
            logger.debug(e.getMessage(), e);
        }

        return Response.status(e.getHttpStatus()).type(MediaType.APPLICATION_JSON).entity(e.getError()).build();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}