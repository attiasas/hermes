package dev.hermes.core.audio;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceCache;
import dev.hermes.core.resource.ResourceKey;
import dev.hermes.core.resource.ResourceLoaderRegistry;
import dev.hermes.core.resource.SyncLoadPipeline;
import dev.hermes.core.resource.loaders.SoundResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SoundResourceLoaderTest {

    @Test
    void loadsSoundViaInjectedBackend() {
        TestGdx.initClasspathFiles();
        RecordingSoundBackend backend = new RecordingSoundBackend();
        ResourceLoaderRegistry registry = ResourceLoaderRegistry.withDefaults(backend);
        ResourceCache cache = new ResourceCache();
        SyncLoadPipeline pipeline = new SyncLoadPipeline(cache, registry);
        ResourceKey key = new ResourceKey(ResourceRef.of("sfx/test.wav"), ResourceKind.SOUND);
        pipeline.loadSync(key, "sfx/test.wav");
        assertTrue(cache.contains(key));
        assertNotNull(backend.loadSound("sfx/test.wav"));
    }

    @Test
    void decodeUploadUsesBackendDirectly() {
        TestGdx.initClasspathFiles();
        RecordingSoundBackend backend = new RecordingSoundBackend();
        SoundResourceLoader loader = new SoundResourceLoader(backend);
        Object handle = loader.upload(loader.decode("sfx/test.wav"));
        assertNotNull(handle);
        assertSame(handle, backend.loadSound("sfx/test.wav"));
    }
}
