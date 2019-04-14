package be.valuya.advantaje.core;

public class AdvantajeException extends RuntimeException {
    public AdvantajeException() {
    }

    public AdvantajeException(String message) {
        super(message);
    }

    public AdvantajeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdvantajeException(Throwable cause) {
        super(cause);
    }

    public AdvantajeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
