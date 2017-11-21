package example.client.exception;

import com.fasterxml.jackson.databind.ObjectMapper;

import example.service.payload.ErrorDetail;

import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.io.InputStream;

public class HelloWorldClientServiceClientMapper implements ResponseExceptionMapper<HelloWorldServiceException> {
    private static Logger logger = LoggerFactory.getLogger(HelloWorldClientServiceClientMapper.class);

    @Override
    public HelloWorldServiceException fromResponse(Response response) {
        InputStream inputStream = null;
        String errorMsg = "remote exception";
        ErrorDetail error = new ErrorDetail(Status.INTERNAL_SERVER_ERROR.name(), errorMsg);
        try {
            Object entity = response.getEntity();
            if (entity instanceof InputStream) {
                inputStream = (InputStream) entity;
                error = new ObjectMapper().readValue(inputStream, ErrorDetail.class);
            }
        } catch (Exception e) {
            logger.error("Failed to parse response from hello world service!", e);
            error.setErrorDescription(e.getMessage());
            error.setError("unexpected_error");

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.debug("Failed to close inputStream!", e);
                }
            }
        }
        final int status = response.getStatus();
        return new HelloWorldServiceException(Status.fromStatusCode(status), error, error.getErrorDescription());

    }

}
