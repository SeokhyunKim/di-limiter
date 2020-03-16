package io.dicounter.overlaynet.exceptions;

public class BadNodeIdException extends RuntimeException {

    public BadNodeIdException() {
        super();
    }

    public BadNodeIdException(final String message) {
        super(message);
    }

    public BadNodeIdException(final Throwable cause) {
        super(cause);
    }

    public BadNodeIdException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
