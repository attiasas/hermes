package dev.hermes.core.animation;

import dev.hermes.api.Component;
import dev.hermes.api.EntityId;
import dev.hermes.api.animation.AnimationTrackResolver;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.LocalTransform;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.MaterialUniform;
import dev.hermes.api.ecs.Transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Applies sampled animation values to ECS components. */
public final class AnimationTargetApplier {

    private static final String TRANSFORM_PREFIX = "Transform.";
    private static final String PARTS_PREFIX = "parts.";
    private static final String MATERIAL_PREFIX = "Material.uniforms.";
    private final List<AnimationTrackResolver> resolvers;

    public AnimationTargetApplier() {
        this(List.of());
    }

    public AnimationTargetApplier(List<AnimationTrackResolver> resolvers) {
        this.resolvers = Objects.requireNonNull(resolvers, "resolvers");
    }

    public void apply(EntityStore store, EntityId entityId, String target, AnimationTrackEvaluator.Value value) {
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(value, "value");

        float scalarValue = value.hasScalar() ? value.scalar() : Float.NaN;
        float[] valueArray = requireArray(value);
        for (AnimationTrackResolver resolver : resolvers) {
            if (resolver != null && resolver.apply(target, scalarValue, valueArray, entityId, store)) {
                return;
            }
        }

        if (target.startsWith(TRANSFORM_PREFIX)) {
            applyTransformTarget(store, entityId, target.substring(TRANSFORM_PREFIX.length()), value);
            return;
        }
        if (target.startsWith(PARTS_PREFIX)) {
            applyPartsTarget(store, entityId, target.substring(PARTS_PREFIX.length()), value);
            return;
        }
        if (target.startsWith(MATERIAL_PREFIX)) {
            applyMaterialTarget(store, entityId, target.substring(MATERIAL_PREFIX.length()), value);
            return;
        }
        throw new IllegalArgumentException("Unsupported animation target: " + target);
    }

    private static void applyTransformTarget(
            EntityStore store, EntityId entityId, String property, AnimationTrackEvaluator.Value value) {
        Transform transform = requireComponent(store, entityId, Transform.class, "Transform");
        float scalar = requireScalar(value);
        switch (property) {
            case "x":
                transform.setX(scalar);
                return;
            case "y":
                transform.setY(scalar);
                return;
            case "z":
                transform.setZ(scalar);
                return;
            case "rotationX":
                transform.setRotationX(scalar);
                return;
            case "rotationY":
                transform.setRotationY(scalar);
                return;
            case "rotationZ":
                transform.setRotationZ(scalar);
                return;
            case "scaleX":
                transform.setScaleX(scalar);
                return;
            case "scaleY":
                transform.setScaleY(scalar);
                return;
            case "scaleZ":
                transform.setScaleZ(scalar);
                return;
            default:
                throw new IllegalArgumentException("Unsupported Transform target property: " + property);
        }
    }

    private static void applyPartsTarget(
            EntityStore store, EntityId entityId, String partsPath, AnimationTrackEvaluator.Value value) {
        int separator = partsPath.indexOf('.');
        if (separator <= 0 || separator >= partsPath.length() - 1) {
            throw new IllegalArgumentException("Invalid parts target path: parts." + partsPath);
        }

        String partId = partsPath.substring(0, separator);
        String suffix = partsPath.substring(separator + 1);

        Drawables drawables = requireComponent(store, entityId, Drawables.class, "Drawables");
        DrawablePart part = findPart(drawables, partId);

        if (suffix.startsWith("local.")) {
            applyLocalTransformTarget(part.local(), suffix.substring("local.".length()), value);
            return;
        }
        if ("frame".equals(suffix)) {
            part.local().setSpriteFrame(Math.round(requireScalar(value)));
            return;
        }
        if ("visible".equals(suffix)) {
            part.local().setVisible(requireScalar(value) != 0f);
            return;
        }

        throw new IllegalArgumentException("Unsupported part target path: parts." + partsPath);
    }

    private static void applyLocalTransformTarget(
            LocalTransform localTransform, String property, AnimationTrackEvaluator.Value value) {
        float scalar = requireScalar(value);
        switch (property) {
            case "x":
                localTransform.setX(scalar);
                return;
            case "y":
                localTransform.setY(scalar);
                return;
            case "z":
                localTransform.setZ(scalar);
                return;
            case "rotationX":
                localTransform.setRotationX(scalar);
                return;
            case "rotationY":
                localTransform.setRotationY(scalar);
                return;
            case "rotationZ":
                localTransform.setRotationZ(scalar);
                return;
            case "scaleX":
                localTransform.setScaleX(scalar);
                return;
            case "scaleY":
                localTransform.setScaleY(scalar);
                return;
            case "scaleZ":
                localTransform.setScaleZ(scalar);
                return;
            default:
                throw new IllegalArgumentException("Unsupported local transform property: " + property);
        }
    }

    private static void applyMaterialTarget(
            EntityStore store, EntityId entityId, String uniformName, AnimationTrackEvaluator.Value value) {
        if (uniformName.isBlank()) {
            throw new IllegalArgumentException("Material target must include a uniform name");
        }
        Material material = requireComponent(store, entityId, Material.class, "Material");
        float[] sampled = requireArray(value);
        MaterialUniform existing = material.uniform(uniformName);
        if (existing != null && existing.getAsFloatArray().length != sampled.length) {
            throw new IllegalArgumentException(
                    "Uniform length mismatch for " + uniformName + ": expected "
                            + existing.getAsFloatArray().length + " got " + sampled.length);
        }

        Map<String, MaterialUniform> updatedUniforms = new HashMap<>(material.uniforms());
        updatedUniforms.put(uniformName, new MaterialUniform(sampled));
        material.setUniforms(updatedUniforms);
    }

    private static float requireScalar(AnimationTrackEvaluator.Value value) {
        if (value.hasScalar()) {
            return value.scalar();
        }
        float[] array = value.array();
        if (array.length == 1) {
            return array[0];
        }
        throw new IllegalArgumentException("Target requires scalar value but sampled value is an array");
    }

    private static float[] requireArray(AnimationTrackEvaluator.Value value) {
        if (value.hasScalar()) {
            return new float[]{value.scalar()};
        }
        return value.array();
    }

    private static DrawablePart findPart(Drawables drawables, String partId) {
        for (DrawablePart part : drawables.parts()) {
            if (part.id().equals(partId)) {
                return part;
            }
        }
        throw new IllegalArgumentException("Unknown drawable part id: " + partId);
    }

    private static <T extends Component> T requireComponent(
            EntityStore store, EntityId entityId, Class<T> componentType, String componentName) {
        T component = store.getComponent(entityId, componentType);
        if (component == null) {
            throw new IllegalArgumentException("Entity " + entityId + " has no " + componentName + " component");
        }
        return component;
    }
}
