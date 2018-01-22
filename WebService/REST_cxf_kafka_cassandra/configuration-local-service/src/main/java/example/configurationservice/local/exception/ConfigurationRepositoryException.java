package example.configurationservice.local.exception;

/**
 * An exception for configuration DB error
 */
public class ConfigurationRepositoryException extends ConfigurationException {

    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2187187506930645381L;

    /**
     * Creates a new instance of
     * <code>WSFConfigurationDbException</code>.
     */
    public ConfigurationRepositoryException() {
        super();
    }

    /**
     * Creates a new instance of
     * <code>WSFConfigurationDbException</code>.
     *
     * @param message the detail message.
     */
    public ConfigurationRepositoryException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of
     * <code>WSFConfigurationDbException</code>.
     *
     * @param cause the cause.
     */
    public ConfigurationRepositoryException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance of
     * <code>WSFConfigurationDbException</code>.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public ConfigurationRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
