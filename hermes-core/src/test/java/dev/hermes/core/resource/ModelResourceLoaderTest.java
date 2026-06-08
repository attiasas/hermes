package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.loaders.ModelResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelResourceLoaderTest {

    @Test
    void loadsModelFromClasspath() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        ResourceCache cache = new ResourceCache();
        SyncLoadPipeline pipeline = new SyncLoadPipeline(cache, ResourceLoaderRegistry.withDefaults());
        ResourceKey key = new ResourceKey(ResourceRef.of("models/cube.obj"), ResourceKind.MODEL);
        pipeline.loadSync(key, "models/cube.obj");
        assertTrue(cache.contains(key));
        assertInstanceOf(Model.class, cache.get(key));
    }

    @Test
    void modelLoaderProducesValidModel() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        ModelResourceLoader loader = new ModelResourceLoader();
        Object model = loader.upload(loader.decode("models/cube.obj"));
        assertNotNull(model);
        assertInstanceOf(Model.class, model);
        loader.dispose(model);
    }
}
