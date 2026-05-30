package dev.hermes.core.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.lighting.SceneLightingNames;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class LightingBudgetResolverTest {

    private ComponentRegistryImpl registry;
    private WorldManagerImpl manager;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        manager = new WorldManagerImpl();
        SceneLoader.loadFromString("s.json", "{\"entities\":[]}", manager.entities(), registry);
    }

    @Test
    void apply_writesSceneLightingStateBudgets() {
        LightingBudgetResolver.apply(manager, "render/pipeline-point-lights.json");

        Entity sceneEntity = manager.entities().findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        assertNotNull(sceneEntity);
        SceneLightingState state =
                manager.entities().getComponent(sceneEntity.id(), SceneLightingState.class);
        assertNotNull(state);
        assertEquals(1, state.maxDirectional());
        assertEquals(4, state.maxPoint());
        assertEquals(0, state.maxSpot());
    }
}
