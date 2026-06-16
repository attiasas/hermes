package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SpritesPassTest {

    private ResourceManagerImpl resources;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        resources = ResourceManagerImpl.createDefault();
    }

    @Test
    void loadSync_resolvesTextureThroughResourceAccess() {
        ResourceRef ref = ResourceRef.of("textures/test-rgba.png");
        resources.loadSync(ref, ResourceKind.TEXTURE);
        assertTrue(resources.isLoaded(ref, ResourceKind.TEXTURE));
        TextureRegion region = ResourceAccess.textureRegion(resources, ref);
        assertNotNull(region);
        assertTrue(region.getRegionWidth() > 0);
        assertTrue(region.getRegionHeight() > 0);
    }
}
