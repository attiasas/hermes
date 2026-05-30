package dev.hermes.core.lighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.math.Vector3;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.AmbientLight;
import dev.hermes.api.ecs.PointLightSpec;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.SpotLightSpec;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Compiles ECS light components and scene defaults into a libGDX {@link Environment}. */
public final class LightEnvironmentBuilder {

    private static final Vector3 SCRATCH_DIRECTION = new Vector3();
    private static final Vector3 SCRATCH_POSITION = new Vector3();

    private LightEnvironmentBuilder() {}

    public static Environment build(
            EntityStore entities,
            SceneLightingState state,
            LightingBudgets budgets,
            Vector3 cameraPosition) {
        Environment environment = new Environment();

        applyAmbient(entities, state, environment);
        applyDirectionalLights(entities, state, budgets, environment);
        applyPointLights(entities, state, budgets, cameraPosition, environment);
        applySpotLights(entities, state, budgets, cameraPosition, environment);

        return environment;
    }

    private static void applyAmbient(
            EntityStore entities, SceneLightingState state, Environment environment) {
        float[] color = state.defaultAmbientColor();
        float intensity = state.defaultAmbientIntensity();

        for (Entity entity : entities.entitiesWith(AmbientLight.class)) {
            AmbientLight light = entities.getComponent(entity.id(), AmbientLight.class);
            if (light == null || !light.enabled()) {
                continue;
            }
            color = light.color();
            intensity = light.intensity();
        }

        ColorAttribute ambient = new ColorAttribute(ColorAttribute.AmbientLight);
        applyIntensityColor(ambient.color, color, intensity);
        environment.set(ambient);
    }

    private static void applyIntensityColor(Color out, float[] color, float intensity) {
        out.r = color[0] * intensity;
        out.g = color.length > 1 ? color[1] * intensity : 0f;
        out.b = color.length > 2 ? color[2] * intensity : 0f;
        out.a = color.length > 3 ? color[3] : 1f;
    }

    private static DirectionalLight newDirectionalLight(float[] color, float intensity, Vector3 direction) {
        DirectionalLight light = new DirectionalLight();
        applyIntensityColor(light.color, color, intensity);
        light.direction.set(direction).nor();
        return light;
    }

    private static PointLight newPointLight(float[] color, float intensity, float x, float y, float z) {
        PointLight light = new PointLight();
        applyIntensityColor(light.color, color, intensity);
        light.position.set(x, y, z);
        light.intensity = 1f;
        return light;
    }

    private static SpotLight newSpotLight(
            float[] color,
            float intensity,
            float x,
            float y,
            float z,
            Vector3 direction,
            float cutoffAngle,
            float exponent) {
        SpotLight light = new SpotLight();
        applyIntensityColor(light.color, color, intensity);
        light.position.set(x, y, z);
        light.direction.set(direction).nor();
        light.intensity = 1f;
        light.cutoffAngle = cutoffAngle;
        light.exponent = exponent;
        return light;
    }

    private static void applyDirectionalLights(
            EntityStore entities,
            SceneLightingState state,
            LightingBudgets budgets,
            Environment environment) {
        int remaining = budgets.maxDirectional();

        if (state.hasDefaultDirectional() && remaining > 0) {
            float[] color = state.defaultDirectionalColor();
            float intensity = state.defaultDirectionalIntensity();
            float[] direction = state.defaultDirectionalDirection();
            SCRATCH_DIRECTION.set(direction[0], direction[1], direction[2]).nor();
            environment.add(newDirectionalLight(color, intensity, SCRATCH_DIRECTION));
            remaining--;
        }

        for (Entity entity : entities.entitiesWith(dev.hermes.api.ecs.DirectionalLight.class)) {
            if (remaining <= 0) {
                break;
            }
            dev.hermes.api.ecs.DirectionalLight light =
                    entities.getComponent(entity.id(), dev.hermes.api.ecs.DirectionalLight.class);
            if (light == null || !light.enabled()) {
                continue;
            }
            resolveDirection(entities, entity, light.directionOverridden(), light.direction(), SCRATCH_DIRECTION);
            environment.add(newDirectionalLight(light.color(), light.intensity(), SCRATCH_DIRECTION));
            remaining--;
        }
    }

    private static void applyPointLights(
            EntityStore entities,
            SceneLightingState state,
            LightingBudgets budgets,
            Vector3 cameraPosition,
            Environment environment) {
        for (PointLightSpec spec : state.scenePointLights()) {
            float[] position = spec.position();
            float[] color = spec.color();
            float intensity = spec.intensity();
            environment.add(
                    newPointLight(
                            color,
                            intensity,
                            position[0],
                            position[1],
                            position[2]));
        }

        int remaining = Math.max(0, budgets.maxPoint() - state.scenePointLights().size());
        if (remaining <= 0) {
            return;
        }

        List<EntityPointLight> candidates = new ArrayList<>();
        for (Entity entity : entities.entitiesWith(dev.hermes.api.ecs.PointLight.class)) {
            dev.hermes.api.ecs.PointLight light =
                    entities.getComponent(entity.id(), dev.hermes.api.ecs.PointLight.class);
            if (light == null || !light.enabled()) {
                continue;
            }
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform == null) {
                transform = new Transform();
            }
            TransformMath.worldPosition(transform, SCRATCH_POSITION);
            float distance = SCRATCH_POSITION.dst(cameraPosition);
            candidates.add(new EntityPointLight(entity, light, transform, distance));
        }

        candidates.sort(Comparator.comparingDouble(EntityPointLight::distance));
        for (int i = 0; i < Math.min(remaining, candidates.size()); i++) {
            EntityPointLight entry = candidates.get(i);
            dev.hermes.api.ecs.PointLight light = entry.light();
            Transform transform = entry.transform();
            float[] color = light.color();
            float intensity = light.intensity();
            TransformMath.worldPosition(transform, SCRATCH_POSITION);
            environment.add(
                    newPointLight(
                            color,
                            intensity,
                            SCRATCH_POSITION.x,
                            SCRATCH_POSITION.y,
                            SCRATCH_POSITION.z));
        }
    }

    private static void applySpotLights(
            EntityStore entities,
            SceneLightingState state,
            LightingBudgets budgets,
            Vector3 cameraPosition,
            Environment environment) {
        for (SpotLightSpec spec : state.sceneSpotLights()) {
            float[] position = spec.position();
            float[] color = spec.color();
            float intensity = spec.intensity();
            float[] direction = spec.direction();
            SCRATCH_DIRECTION.set(direction[0], direction[1], direction[2]).nor();
            environment.add(
                    newSpotLight(
                            color,
                            intensity,
                            position[0],
                            position[1],
                            position[2],
                            SCRATCH_DIRECTION,
                            spec.cutoffAngle(),
                            spec.exponent()));
        }

        int remaining = Math.max(0, budgets.maxSpot() - state.sceneSpotLights().size());
        if (remaining <= 0) {
            return;
        }

        List<EntitySpotLight> candidates = new ArrayList<>();
        for (Entity entity : entities.entitiesWith(dev.hermes.api.ecs.SpotLight.class)) {
            dev.hermes.api.ecs.SpotLight light =
                    entities.getComponent(entity.id(), dev.hermes.api.ecs.SpotLight.class);
            if (light == null || !light.enabled()) {
                continue;
            }
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform == null) {
                transform = new Transform();
            }
            TransformMath.worldPosition(transform, SCRATCH_POSITION);
            float distance = SCRATCH_POSITION.dst(cameraPosition);
            candidates.add(new EntitySpotLight(entity, light, transform, distance));
        }

        candidates.sort(Comparator.comparingDouble(EntitySpotLight::distance));
        for (int i = 0; i < Math.min(remaining, candidates.size()); i++) {
            EntitySpotLight entry = candidates.get(i);
            dev.hermes.api.ecs.SpotLight light = entry.light();
            Transform transform = entry.transform();
            float[] color = light.color();
            float intensity = light.intensity();
            TransformMath.worldPosition(transform, SCRATCH_POSITION);
            resolveDirection(entities, entry.entity(), light.directionOverridden(), light.direction(), SCRATCH_DIRECTION);
            environment.add(
                    newSpotLight(
                            color,
                            intensity,
                            SCRATCH_POSITION.x,
                            SCRATCH_POSITION.y,
                            SCRATCH_POSITION.z,
                            SCRATCH_DIRECTION,
                            light.cutoffAngle(),
                            light.exponent()));
        }
    }

    private static void resolveDirection(
            EntityStore entities,
            Entity entity,
            boolean directionOverridden,
            float[] componentDirection,
            Vector3 out) {
        if (directionOverridden) {
            out.set(componentDirection[0], componentDirection[1], componentDirection[2]).nor();
            return;
        }
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        if (transform == null) {
            out.set(componentDirection[0], componentDirection[1], componentDirection[2]).nor();
            return;
        }
        TransformMath.worldNegZ(transform, out);
    }

    private static final class EntityPointLight {
        private final Entity entity;
        private final dev.hermes.api.ecs.PointLight light;
        private final Transform transform;
        private final float distance;

        private EntityPointLight(
                Entity entity, dev.hermes.api.ecs.PointLight light, Transform transform, float distance) {
            this.entity = entity;
            this.light = light;
            this.transform = transform;
            this.distance = distance;
        }

        private Entity entity() {
            return entity;
        }

        private dev.hermes.api.ecs.PointLight light() {
            return light;
        }

        private Transform transform() {
            return transform;
        }

        private float distance() {
            return distance;
        }
    }

    private static final class EntitySpotLight {
        private final Entity entity;
        private final dev.hermes.api.ecs.SpotLight light;
        private final Transform transform;
        private final float distance;

        private EntitySpotLight(
                Entity entity, dev.hermes.api.ecs.SpotLight light, Transform transform, float distance) {
            this.entity = entity;
            this.light = light;
            this.transform = transform;
            this.distance = distance;
        }

        private Entity entity() {
            return entity;
        }

        private dev.hermes.api.ecs.SpotLight light() {
            return light;
        }

        private Transform transform() {
            return transform;
        }

        private float distance() {
            return distance;
        }
    }
}
