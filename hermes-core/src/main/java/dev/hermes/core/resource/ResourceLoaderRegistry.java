package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.core.audio.AudioBackends;
import dev.hermes.core.audio.SoundBackend;
import dev.hermes.core.resource.loaders.ModelResourceLoader;
import dev.hermes.core.resource.loaders.SoundResourceLoader;
import dev.hermes.core.resource.loaders.TextureResourceLoader;
import dev.hermes.core.world.tilemap.HermesTileMapLoader;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Maps {@link ResourceKind} to {@link ResourceLoader} implementations. */
public final class ResourceLoaderRegistry implements dev.hermes.api.resource.ResourceLoaderRegistry {

    private final Map<ResourceKind, ResourceLoader> loaders = new EnumMap<>(ResourceKind.class);

    @Override
    public void register(ResourceKind kind, dev.hermes.api.resource.ResourceLoader loader) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(loader, "loader");
        if (!(loader instanceof ResourceLoader)) {
            throw new IllegalArgumentException(
                    "loader must implement dev.hermes.core.resource.ResourceLoader");
        }
        ResourceLoader coreLoader = (ResourceLoader) loader;
        if (coreLoader.kind() != kind) {
            throw new IllegalArgumentException(
                    "Loader kind " + coreLoader.kind() + " does not match registration kind " + kind);
        }
        loaders.put(kind, coreLoader);
    }

    public ResourceLoader require(ResourceKind kind) {
        ResourceLoader loader = loaders.get(kind);
        if (loader == null) {
            throw new ResourceLoadException("No loader registered for kind: " + kind);
        }
        return loader;
    }

    public static ResourceLoaderRegistry withDefaults() {
        return withDefaults(AudioBackends.gdx());
    }

    public static ResourceLoaderRegistry withDefaults(SoundBackend soundBackend) {
        Objects.requireNonNull(soundBackend, "soundBackend");
        ResourceLoaderRegistry registry = new ResourceLoaderRegistry();
        registry.register(ResourceKind.TEXTURE, new TextureResourceLoader());
        registry.register(ResourceKind.MODEL, new ModelResourceLoader());
        registry.register(ResourceKind.SOUND, new SoundResourceLoader(soundBackend));
        registry.register(ResourceKind.TILEMAP, new HermesTileMapLoader());
        return registry;
    }
}
