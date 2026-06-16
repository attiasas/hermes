package dev.hermes.api.resource;

/** SPI hook for registering custom resource loaders. */
public interface ResourceLoaderRegistration {

    void register(ResourceLoaderRegistry registry);
}
