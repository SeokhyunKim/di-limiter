package dicounter.overlaynet.exception;

public class NetworkException extends RuntimeException {

    public NetworkException() {
        super();
    }

    public NetworkException(final String message) {
        super(message);
    }

    public NetworkException(final Throwable cause) {
        super(cause);
    }

    public NetworkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
