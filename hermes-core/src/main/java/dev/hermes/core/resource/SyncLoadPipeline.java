package dev.hermes.core.resource;

import java.util.Objects;

/** Blocking load path: decode and upload on the calling thread. */
public final class SyncLoadPipeline {

    private final ResourceCache cache;
    private final ResourceLoaderRegistry registry;

    public SyncLoadPipeline(ResourceCache cache, ResourceLoaderRegistry registry) {
        this.cache = Objects.requireNonNull(cache, "cache");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public void loadSync(ResourceKey key, String resolvedPath) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(resolvedPath, "resolvedPath");
        ResourceLoader loader = registry.require(key.kind());
        DecodedPayload decoded = loader.decode(resolvedPath);
        Object resource = loader.upload(decoded);
        cache.put(key, resource, () -> loader.dispose(resource));
    }
}
