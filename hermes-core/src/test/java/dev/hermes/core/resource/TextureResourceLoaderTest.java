package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TextureResourceLoaderTest {

    @Test
    void loadsTextureFromClasspath() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        ResourceCache cache = new ResourceCache();
        SyncLoadPipeline pipeline = new SyncLoadPipeline(cache, ResourceLoaderRegistry.withDefaults());
        ResourceKey key = new ResourceKey(ResourceRef.of("textures/test-rgba.png"), ResourceKind.TEXTURE);
        pipeline.loadSync(key, "textures/test-rgba.png");
        assertTrue(cache.contains(key));
    }
}
