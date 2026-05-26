package dev.hermes.core.input;

/** Thrown when an input profile JSON document is invalid. */
public final class InputProfileParseException extends RuntimeException {

    public InputProfileParseException(String message) {
        super(message);
    }

    public InputProfileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
