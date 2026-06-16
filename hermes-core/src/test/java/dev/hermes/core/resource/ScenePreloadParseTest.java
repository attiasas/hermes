package dev.hermes.core.resource;

import dev.hermes.core.ecs.SceneLoadMetadata;
import dev.hermes.core.ecs.SceneLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenePreloadParseTest {

    @Test
    void parsesPreloadFromSceneJson() {
        SceneLoadMetadata meta =
                SceneLoader.loadMetadataFromString(
                        "{\"preload\":{\"bundles\":[\"main-menu\"],\"async\":true},\"entities\":[]}");
        assertTrue(meta.preload().isPresent());
        assertEquals(List.of("main-menu"), meta.preload().get().bundles());
        assertTrue(meta.preload().get().async());
    }
}
