package dicounter.overlaynet.exception;

public class BadNodeAddressException extends RuntimeException {

    public BadNodeAddressException() {
        super();
    }

    public BadNodeAddressException(final String message) {
        super(message);
    }

    public BadNodeAddressException(final Throwable cause) {
        super(cause);
    }

    public BadNodeAddressException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
