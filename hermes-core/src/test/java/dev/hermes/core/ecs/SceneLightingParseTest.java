package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.lighting.SceneLightingNames;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SceneLightingParseTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void sceneLighting_populatesSceneLightingState() {
        String json =
                "{\n"
                        + "  \"lighting\": {\n"
                        + "    \"version\": 1,\n"
                        + "    \"ambient\": { \"color\": [0.1, 0.1, 0.2, 1] }\n"
                        + "  },\n"
                        + "  \"entities\": []\n"
                        + "}\n";
        WorldManagerImpl manager = new WorldManagerImpl();
        SceneLoader.loadFromString("s.json", json, manager.entities(), registry);
        Entity sceneEntity = manager.entities().findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        assertNotNull(sceneEntity);
        SceneLightingState state =
                manager.entities().getComponent(sceneEntity.id(), SceneLightingState.class);
        assertEquals(0.1f, state.defaultAmbientColor()[0], 0.001f);
    }

    @Test
    void sceneEntity_reservedNameRejected() {
        String json = "{\"entities\":[{\"id\":\"__hermes_scene__\",\"components\":{}}]}";
        WorldManagerImpl manager = new WorldManagerImpl();
        assertThrows(
                SceneParseException.class,
                () -> SceneLoader.loadFromString("bad.json", json, manager.entities(), registry));
    }

    @Test
    void emptyScene_createsSceneEntityWithEngineDefaults() {
        String json = "{\"entities\":[]}";
        WorldManagerImpl manager = new WorldManagerImpl();
        SceneLoader.loadFromString("empty.json", json, manager.entities(), registry);
        Entity sceneEntity = manager.entities().findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        assertNotNull(sceneEntity);
        SceneLightingState state =
                manager.entities().getComponent(sceneEntity.id(), SceneLightingState.class);
        assertEquals(0.4f, state.defaultAmbientColor()[0], 0.001f);
        assertTrue(state.hasDefaultDirectional());
        assertEquals(1, state.maxDirectional());
    }
}
