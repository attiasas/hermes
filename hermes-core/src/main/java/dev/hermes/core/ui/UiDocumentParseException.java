package dev.hermes.core.ui;

/** Thrown when a UI document JSON file is invalid. */
public final class UiDocumentParseException extends RuntimeException {

    public UiDocumentParseException(String message) {
        super(message);
    }

    public UiDocumentParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
