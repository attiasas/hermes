package dev.hermes.api.resource;

/**
 * Marker for {@link ResourceKind}-specific loaders.
 * <p>Full decode/upload contract lives in {@code dev.hermes.core.resource.ResourceLoader}.
 */
public interface ResourceLoader {

    ResourceKind kind();
}
