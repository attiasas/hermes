package dev.hermes.core.render.pass;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.DrawableKind;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.DrawableRig;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.MaterialUniform;
import dev.hermes.api.ecs.PartMaterial;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.lighting.LightingRuntime;
import dev.hermes.core.animation.RigInstanceCache;
import dev.hermes.core.render.ShaderCompileException;
import dev.hermes.core.render.TransformComposer;
import dev.hermes.core.render.resource.MaterialUniformBinder;
import dev.hermes.core.render.resource.ShaderRegistry;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.viewport.BoundCamera;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Renders mesh entities in entities space with the active scene camera.
 */
public final class World3dPass {

    private final ResourceManagerImpl resources;
    private final ShaderRegistry shaderRegistry;
    private final RigInstanceCache rigInstances;
    private final ModelBatch modelBatch;
    private final Map<String, Shader> g3dShaderCache = new HashMap<>();
    private final RenderablePool renderablePool = new RenderablePool();
    private final Array<Renderable> renderableScratch = new Array<>(1);

    public World3dPass(ResourceManagerImpl resources) {
        this(resources, null);
    }

    public World3dPass(ResourceManagerImpl resources, ShaderRegistry shaderRegistry) {
        this.resources = Objects.requireNonNull(resources, "resources");
        this.shaderRegistry = shaderRegistry;
        this.rigInstances = RigInstanceCache.shared(resources);
        this.modelBatch = new ModelBatch();
    }

    public void resize(int width, int height) {
    }

    public void render(EntityStore entities) {
        throw new UnsupportedOperationException("Use render(EntityStore, layers, BoundCamera)");
    }

    public void render(EntityStore entities, Set<RenderLayer.Layer> layers, BoundCamera bound) {
        List<Entity> drawables = collectDrawables(entities, layers);
        if (drawables.isEmpty()) {
            return;
        }

        Environment environment = LightingRuntime.require(entities);
        modelBatch.begin(bound.gdxCamera());
        for (Entity entity : drawables) {
            drawMesh(entities, entity, environment);
        }
        modelBatch.end();
    }

    private void drawMesh(EntityStore entities, Entity entity, Environment environment) {
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        Drawables drawables = entities.getComponent(entity.id(), Drawables.class);
        Material entityMaterial = entities.getComponent(entity.id(), Material.class);
        if (transform == null || drawables == null || entityMaterial == null) {
            return;
        }

        for (DrawablePart part : drawables.parts()) {
            if (!shouldDrawMeshPart(part)) {
                continue;
            }
            Material material = resolveMaterial(entityMaterial, part);
            String modelPath = resolveModelPath(part);

            String shaderId = material.shader();
            if (shaderRegistry != null && !shaderRegistry.isRegistered(shaderId)) {
                throw new ShaderCompileException("shader not registered: " + shaderId);
            }

            ResourceRef ref = ResourceRef.of(modelPath);
            ModelInstance instance;
            if (part.rig() == DrawableRig.GLTF) {
                instance =
                        rigInstances
                                .getOrCreate(entity.id(), part.id(), modelPath, resources)
                                .instance();
            } else {
                resources.loadSync(ref, ResourceKind.MODEL);
                instance = new ModelInstance(ResourceAccess.model(resources, ref));
            }
            TransformComposer.composeInto(instance.transform, transform, part.local());

            Shader g3dShader = resolveG3dShader(shaderId, instance, environment);
            if (g3dShader != null) {
                applyMaterialUniforms(g3dShader, material);
                modelBatch.render(instance, environment, g3dShader);
            } else {
                modelBatch.render(instance, environment);
            }
        }
    }

    static boolean shouldDrawMeshPart(DrawablePart part) {
        if (!part.local().visible() || part.kind() != DrawableKind.MESH) {
            return false;
        }
        String modelPath = resolveModelPath(part);
        return modelPath != null && !modelPath.isBlank();
    }

    private static Material resolveMaterial(Material entityMaterial, DrawablePart part) {
        PartMaterial override = part.partMaterial();
        if (override == null) {
            return entityMaterial;
        }
        Material resolved = new Material();
        String shader = override.shader();
        resolved.setShader(shader != null && !shader.isBlank() ? shader : entityMaterial.shader());
        if (override.uniforms().isEmpty()) {
            resolved.setUniforms(entityMaterial.uniforms());
        } else {
            Map<String, MaterialUniform> merged = new HashMap<>(entityMaterial.uniforms());
            merged.putAll(override.uniforms());
            resolved.setUniforms(merged);
        }
        return resolved;
    }

    private static String resolveModelPath(DrawablePart part) {
        return part.model();
    }

    private Shader resolveG3dShader(String shaderId, ModelInstance instance, Environment environment) {
        if (shaderRegistry == null || shaderRegistry.usesBuiltin(shaderId)) {
            return null;
        }
        return g3dShaderCache.computeIfAbsent(
                shaderId,
                id -> {
                    Renderable renderable = firstRenderable(instance);
                    return shaderRegistry.resolveG3dShader(id, renderable, environment);
                });
    }

    private Renderable firstRenderable(ModelInstance instance) {
        renderableScratch.clear();
        instance.getRenderables(renderableScratch, renderablePool);
        if (renderableScratch.size == 0) {
            throw new IllegalStateException("model has no renderables");
        }
        return renderableScratch.first();
    }

    private static void applyMaterialUniforms(Shader g3dShader, Material material) {
        if (g3dShader instanceof DefaultShader) {
            MaterialUniformBinder.apply(((DefaultShader) g3dShader).program, material);
        }
    }

    /**
     * Returns mesh entities that have transform and material (excludes camera entities).
     */
    public static List<Entity> collectDrawables(EntityStore entities) {
        return collectDrawables(entities, EnumSet.of(RenderLayer.Layer.WORLD));
    }

    public static List<Entity> collectDrawables(EntityStore entities, Set<RenderLayer.Layer> layers) {
        Set<RenderLayer.Layer> allowed =
                layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.WORLD) : layers;
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities.entitiesWith(Drawables.class)) {
            if (entities.hasComponent(entity.id(), Camera.class)) {
                continue;
            }
            if (!entities.hasComponent(entity.id(), Transform.class)) {
                continue;
            }
            if (!entities.hasComponent(entity.id(), Material.class)) {
                continue;
            }
            Drawables drawables = entities.getComponent(entity.id(), Drawables.class);
            if (!hasAnyMeshPart(drawables)) {
                continue;
            }
            RenderLayer renderLayer = entities.getComponent(entity.id(), RenderLayer.class);
            RenderLayer.Layer layer =
                    renderLayer == null ? RenderLayer.Layer.WORLD : renderLayer.layer();
            if (!allowed.contains(layer)) {
                continue;
            }
            result.add(entity);
        }
        return result;
    }

    private static boolean hasAnyMeshPart(Drawables drawables) {
        if (drawables == null) {
            return false;
        }
        for (DrawablePart part : drawables.parts()) {
            if (shouldDrawMeshPart(part)) {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        for (Shader shader : g3dShaderCache.values()) {
            shader.dispose();
        }
        g3dShaderCache.clear();
        modelBatch.dispose();
    }

    private static final class RenderablePool extends FlushablePool<Renderable> {
        @Override
        protected Renderable newObject() {
            return new Renderable();
        }
    }
}
