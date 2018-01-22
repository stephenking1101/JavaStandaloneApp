package example.configurationservice.local.type;

import example.configuration.type.Configuration;

public class UnexistedConfiguration extends Configuration {

    private static final long serialVersionUID = 6154499314579820913L;

    private String errorMsg;

    private Exception exception;

    public UnexistedConfiguration(String errorMsg) {
        this(errorMsg, null);
    }

    public UnexistedConfiguration(String errorMsg, Exception e) {
        this.exception = e;
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public Exception getException() {
        return exception;
    }
}
