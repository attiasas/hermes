package dev.hermes.core.ecs;

/**
 * Raised when a scene file cannot be loaded or parsed.
 */
public final class SceneLoadException extends RuntimeException {

    public SceneLoadException(String message) {
        super(message);
    }

    public SceneLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
