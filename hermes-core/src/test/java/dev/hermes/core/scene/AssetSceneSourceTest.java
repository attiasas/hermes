package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class AssetSceneSourceTest {

    private WorldManagerImpl manager;
    private ComponentRegistry registry;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        manager = new WorldManagerImpl();
        registry = new HermesEngineImpl().registry();
    }

    @Test
    void populateCreatesEntitiesFromAsset() {
        AssetSceneSource source = new AssetSceneSource("scenes/main.json");
        source.populate(sceneLoadContext(manager, registry));

        EntityStore entities = manager.entities();
        assertEquals(1, entities.entityCount());
        Entity logo = entities.findByName("logo");
        assertNotNull(logo);
        assertEquals(140f, entities.getComponent(logo.id(), Transform.class).x());
        assertEquals(210f, entities.getComponent(logo.id(), Transform.class).y());
        assertEquals("hermes-logo.png", entities.getComponent(logo.id(), Sprite.class).texture());
    }

    private static SceneLoadContext sceneLoadContext(WorldManager manager, ComponentRegistry registry) {
        return new SceneLoadContext() {
            @Override
            public WorldManager manager() {
                return manager;
            }

            @Override
            public ComponentRegistry registry() {
                return registry;
            }
        };
    }
}
