package dev.hermes.core.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class ResourceLoaderRegistrationTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void serviceLoaderRegistersBinaryLoader() {
        HermesEngineImpl engine = new HermesEngineImpl();
        ResourceManagerImpl resources = (ResourceManagerImpl) engine.resources();
        ResourceLoader loader = resources.loaderRegistry().require(ResourceKind.BINARY);
        assertInstanceOf(TestBinaryResourceLoader.class, loader);
    }

    @Test
    void spiBinaryLoaderLoadsCustomExtension() {
        HermesEngineImpl engine = new HermesEngineImpl();
        ResourceManagerImpl resources = (ResourceManagerImpl) engine.resources();
        ResourceRef ref = ResourceRef.of("data/test.bin");
        resources.loadSync(ref, ResourceKind.BINARY);
        assertTrue(resources.isLoaded(ref, ResourceKind.BINARY));
        byte[] payload = (byte[]) resources.cache().get(new ResourceKey(ref, ResourceKind.BINARY));
        assertEquals("HERMES", new String(payload, 0, 6, StandardCharsets.US_ASCII));
    }
}
