package example.configurationservice.local.ut.util;

public class TestPrepareFailedException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 117638439671478491L;

	public TestPrepareFailedException() {
        super();
    }

    public TestPrepareFailedException(String message) {
        super(message);
    }

    public TestPrepareFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestPrepareFailedException(Throwable cause) {
        super(cause);
    }

}
