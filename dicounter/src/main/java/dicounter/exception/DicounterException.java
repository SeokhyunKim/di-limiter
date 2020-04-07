package dicounter.exception;

public class DicounterException extends RuntimeException {

    public DicounterException() {
        super();
    }

    public DicounterException(final String message) {
        super(message);
    }

    public DicounterException(final Throwable cause) {
        super(cause);
    }

    public DicounterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
