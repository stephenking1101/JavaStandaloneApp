package example.client.exception;

import javax.ws.rs.core.Response.Status;

import example.service.payload.ErrorDetail;

public class HelloWorldServiceException extends RuntimeException {
    private static final long serialVersionUID = 3867638276637067905L;

    private Status statusCode = Status.INTERNAL_SERVER_ERROR;

    private final ErrorDetail error;

    public HelloWorldServiceException(ErrorDetail error, Throwable cause) {
        super(cause);
        this.error = error;
    }

    public HelloWorldServiceException(Status statusCode, ErrorDetail error, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.error = error;
    }

    public HelloWorldServiceException(ErrorDetail error, String message) {
        super(message);
        this.error = error;
    }

    public HelloWorldServiceException(Status statusCode, ErrorDetail error, String message) {
        this(error, message);
        this.statusCode = statusCode;
    }

    public HelloWorldServiceException(Status statusCode, ErrorDetail error) {
        this(statusCode, error, error.getErrorDescription());
    }

    public Status getStatusCode() {
        return statusCode;
    }

    public ErrorDetail getError() {
        return error;
    }
}
