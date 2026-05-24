package dev.hermes.core.render;

/**
 * Thrown when a render pipeline JSON document is invalid or unsupported.
 */
public final class PipelineParseException extends RuntimeException {

    public PipelineParseException(String message) {
        super(message);
    }

    public PipelineParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
