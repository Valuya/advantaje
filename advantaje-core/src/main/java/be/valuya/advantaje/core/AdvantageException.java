package be.valuya.advantaje.core;

public class AdvantageException extends RuntimeException {
    public AdvantageException() {
    }

    public AdvantageException(String message) {
        super(message);
    }

    public AdvantageException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdvantageException(Throwable cause) {
        super(cause);
    }

    public AdvantageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
