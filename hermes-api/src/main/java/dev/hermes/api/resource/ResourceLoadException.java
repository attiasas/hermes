package dev.hermes.api.resource;

public final class ResourceLoadException extends RuntimeException {
    public ResourceLoadException(String message) {
        super(message);
    }

    public ResourceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
