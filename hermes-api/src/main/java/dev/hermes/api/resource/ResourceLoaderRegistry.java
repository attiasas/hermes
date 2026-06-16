package dev.hermes.api.resource;

/**
 * Registry for {@link ResourceKind}-specific loaders.
 * <p>Implemented in hermes-core; exposed here for SPI registration signatures.
 */
public interface ResourceLoaderRegistry {

    void register(ResourceKind kind, ResourceLoader loader);
}
