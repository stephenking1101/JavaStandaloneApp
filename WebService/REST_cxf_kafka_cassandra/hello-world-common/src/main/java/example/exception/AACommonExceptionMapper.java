package example.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AACommonExceptionMapper implements ExceptionMapper<AACommonException> {
    private Logger logger = LoggerFactory.getLogger(AACommonExceptionMapper.class);

    public AACommonExceptionMapper() {
    }

    public Response toResponse(AACommonException e) {
        if (e.getHttpStatus() >= Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            this.logger.error(e.getMessage(), e);
        } else {
            this.logger.debug(e.getMessage(), e);
        }

        return Response.status(e.getHttpStatus()).type("application/json").entity(e.getError()).build();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}