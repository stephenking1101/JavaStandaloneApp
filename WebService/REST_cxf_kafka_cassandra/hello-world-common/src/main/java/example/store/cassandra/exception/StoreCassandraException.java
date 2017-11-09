package example.store.cassandra.exception;

public class StoreCassandraException extends RuntimeException {
    private static final long serialVersionUID = -6986682596983073675L;

    public StoreCassandraException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreCassandraException(String message) {
        super(message);
    }

    public StoreCassandraException(Throwable cause) {
        super(cause);
    }
}
