package dev.hermes.core.audio;

/** Thrown when an audio profile asset cannot be parsed. */
public final class AudioProfileParseException extends RuntimeException {

    public AudioProfileParseException(String message) {
        super(message);
    }

    public AudioProfileParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
