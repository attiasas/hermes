package dev.hermes.core.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.math.Vector3;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.AmbientLight;
import dev.hermes.api.ecs.DirectionalLight;
import dev.hermes.api.ecs.PointLight;
import dev.hermes.api.ecs.PointLightSpec;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.lighting.SceneLightingNames;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class LightEnvironmentBuilderTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void build_includesDirectionalFromEntityTransform() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore entities = manager.entities();
        SceneLoader.loadFromString("s.json", "{\"entities\":[]}", entities, registry);

        Entity sun = entities.create("sun");
        Transform transform = new Transform();
        transform.setRotationX(-90f);
        entities.addComponent(sun.id(), transform);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(1, 1, 1, 1);
        entities.addComponent(sun.id(), dl);

        SceneLightingState state = sceneState(entities);
        state.setHasDefaultDirectional(false);

        Vector3 camPos = cameraPosition(entities);
        Environment env =
                LightEnvironmentBuilder.build(
                        entities, state, LightingBudgets.defaults(), camPos);
        DirectionalLightsAttribute directionals =
                (DirectionalLightsAttribute) env.get(DirectionalLightsAttribute.Type);
        assertEquals(1, directionals.lights.size);

        com.badlogic.gdx.graphics.g3d.environment.DirectionalLight gdx = directionals.lights.first();
        assertEquals(0f, gdx.direction.x, 0.01f);
        assertEquals(-1f, gdx.direction.y, 0.01f);
        assertEquals(0f, gdx.direction.z, 0.01f);
    }

    @Test
    void build_ambientFromLastEnabledEntity() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore entities = manager.entities();
        SceneLoader.loadFromString("s.json", "{\"entities\":[]}", entities, registry);

        Entity fillA = entities.create("fill-a");
        AmbientLight ambientA = new AmbientLight();
        ambientA.setColor(1f, 0f, 0f, 1f);
        entities.addComponent(fillA.id(), ambientA);

        Entity fillB = entities.create("fill-b");
        AmbientLight ambientB = new AmbientLight();
        ambientB.setColor(0f, 1f, 0f, 1f);
        ambientB.setIntensity(2f);
        entities.addComponent(fillB.id(), ambientB);

        SceneLightingState state = sceneState(entities);
        Environment env =
                LightEnvironmentBuilder.build(
                        entities, state, LightingBudgets.defaults(), cameraPosition(entities));

        ColorAttribute ambient =
                (ColorAttribute) env.get(ColorAttribute.AmbientLight);
        assertNotNull(ambient);
        assertEquals(0f, ambient.color.r, 0.001f);
        assertEquals(2f, ambient.color.g, 0.001f);
        assertEquals(0f, ambient.color.b, 0.001f);
    }

    @Test
    void build_includesStaticPointLightsFromState() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore entities = manager.entities();
        SceneLoader.loadFromString("s.json", "{\"entities\":[]}", entities, registry);

        SceneLightingState state = sceneState(entities);
        state.setScenePointLights(
                java.util.List.of(
                        new PointLightSpec(
                                new float[] {1f, 2f, 3f},
                                new float[] {1f, 0.5f, 0.2f, 1f},
                                1.5f,
                                12f)));

        LightingBudgets budgets = new LightingBudgets(1, 4, 0);
        Environment env =
                LightEnvironmentBuilder.build(
                        entities, state, budgets, cameraPosition(entities));

        PointLightsAttribute points = (PointLightsAttribute) env.get(PointLightsAttribute.Type);
        assertEquals(1, points.lights.size);
        assertEquals(1f, points.lights.first().position.x, 0.001f);
        assertEquals(2f, points.lights.first().position.y, 0.001f);
        assertEquals(3f, points.lights.first().position.z, 0.001f);
        assertEquals(1.5f, points.lights.first().color.r, 0.001f);
        assertEquals(0.75f, points.lights.first().color.g, 0.001f);
        assertEquals(0.3f, points.lights.first().color.b, 0.001f);
    }

    @Test
    void build_cullsFurthestEntityPointLightsWhenOverBudget() {
        WorldManagerImpl manager = new WorldManagerImpl();
        EntityStore entities = manager.entities();
        SceneLoader.loadFromString("s.json", "{\"entities\":[]}", entities, registry);

        Entity near = entities.create("near");
        Transform nearTransform = new Transform(0f, 0f, 1f);
        entities.addComponent(near.id(), nearTransform);
        PointLight nearLight = new PointLight();
        nearLight.setColor(1f, 0f, 0f, 1f);
        entities.addComponent(near.id(), nearLight);

        Entity far = entities.create("far");
        Transform farTransform = new Transform(0f, 0f, 100f);
        entities.addComponent(far.id(), farTransform);
        PointLight farLight = new PointLight();
        farLight.setColor(0f, 1f, 0f, 1f);
        entities.addComponent(far.id(), farLight);

        SceneLightingState state = sceneState(entities);
        LightingBudgets budgets = new LightingBudgets(1, 1, 0);
        Vector3 camPos = new Vector3(0f, 0f, 0f);

        Environment env = LightEnvironmentBuilder.build(entities, state, budgets, camPos);

        PointLightsAttribute points = (PointLightsAttribute) env.get(PointLightsAttribute.Type);
        assertEquals(1, points.lights.size);
        assertEquals(1f, points.lights.first().color.r, 0.001f);
        assertEquals(0f, points.lights.first().color.g, 0.001f);
    }

    private static SceneLightingState sceneState(EntityStore entities) {
        Entity sceneEntity = entities.findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        return entities.getComponent(sceneEntity.id(), SceneLightingState.class);
    }

    private static Vector3 cameraPosition(EntityStore entities) {
        var cam = CameraResolver.resolve(entities, 800, 600);
        return new Vector3(cam.x(), cam.y(), cam.z());
    }
}
