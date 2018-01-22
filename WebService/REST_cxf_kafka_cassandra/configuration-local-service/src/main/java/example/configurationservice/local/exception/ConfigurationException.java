package example.configurationservice.local.exception;

/**
 * A general Exception for Configuration issues.
 */
public class ConfigurationException extends Exception {
    /**
     * Generated <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2834877533385923668L;

    /**
     * Creates a new <code>WSFException</code> object.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Creates a new <code>WSFException</code> object.
     *
     * @param message the detail message.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>WSFException</code> object.
     *
     * @param cause the cause.
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new <code>WSFException</code> object.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
