package dev.hermes.core.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.PointLight;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.lighting.SceneLightingNames;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class BuiltinLightingSystemTest {

    private ComponentRegistryImpl registry;
    private WorldManagerImpl manager;
    private EntityStore entities;
    private BuiltinLightingSystem system;
    private Entity torch;
    private Transform torchTransform;

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        Gdx.graphics = new ResizableMockGraphics(800, 600);
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        manager = new WorldManagerImpl();
        entities = manager.entities();
        SceneLoader.loadFromString("s.json", "{\"entities\":[]}", entities, registry);
        system = new BuiltinLightingSystem();

        torch = entities.create("torch");
        torchTransform = new Transform(0f, 0f, 5f);
        entities.addComponent(torch.id(), torchTransform);
        entities.addComponent(torch.id(), new PointLight());

        SceneLightingState state = sceneState(entities);
        state.setMaxPoint(4);
    }

    @Test
    void update_incrementsRevisionAndPublishesEnvironment() {
        SceneLightingState state = sceneState(entities);
        assertEquals(0, state.revision());

        system.update(manager, 0.016f);
        assertEquals(1, state.revision());

        Environment env = LightingRuntime.require(entities);
        assertNotNull(env);
        PointLightsAttribute points = (PointLightsAttribute) env.get(PointLightsAttribute.Type);
        assertEquals(1, points.lights.size);
        assertEquals(5f, points.lights.first().position.z, 0.001f);

        torchTransform.setZ(20f);
        system.update(manager, 0.016f);
        assertEquals(2, state.revision());

        Environment envAfterMove = LightingRuntime.require(entities);
        PointLightsAttribute pointsAfterMove =
                (PointLightsAttribute) envAfterMove.get(PointLightsAttribute.Type);
        assertEquals(20f, pointsAfterMove.lights.first().position.z, 0.001f);
    }

    private static SceneLightingState sceneState(EntityStore entities) {
        Entity sceneEntity = entities.findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        return entities.getComponent(sceneEntity.id(), SceneLightingState.class);
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private final int width;
        private final int height;

        ResizableMockGraphics(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getBackBufferWidth() {
            return width;
        }

        @Override
        public int getBackBufferHeight() {
            return height;
        }
    }
}
