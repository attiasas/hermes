package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerSyncLoadTest {

    @Test
    void loadBundleSyncMarksAllLoaded() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        ResourceManagerImpl mgr = ResourceManagerImpl.forTest("assets/resources/");
        mgr.loadBundleSync("test-bundle");
        assertTrue(mgr.isLoaded(ResourceRef.of("@logo"), ResourceKind.TEXTURE));
    }
}
