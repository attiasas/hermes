package dev.hermes.core.render.pass;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.SpriteDrawOrder;
import dev.hermes.core.render.ShaderCompileException;
import dev.hermes.core.render.resource.MaterialUniformBinder;
import dev.hermes.core.render.resource.ShaderRegistry;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.viewport.BoundCamera;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Renders sprites in entities space using the bound scene camera projection.
 */
public final class SpritesPass {

    private final SpriteBatch batch;
    private final ShaderRegistry shaderRegistry;
    private final ResourceManagerImpl resources;
    private final Vector3 projected = new Vector3();

    public SpritesPass(SpriteBatch batch, ResourceManagerImpl resources) {
        this(batch, null, resources);
    }

    public SpritesPass(SpriteBatch batch, ShaderRegistry shaderRegistry, ResourceManagerImpl resources) {
        this.batch = batch;
        this.shaderRegistry = shaderRegistry;
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    public void resize(int width, int height) {
    }

    public void render(EntityStore entities) {
        throw new UnsupportedOperationException("Use render(EntityStore, layers, BoundCamera)");
    }

    public void render(EntityStore entities, Set<RenderLayer.Layer> layers, BoundCamera bound) {
        ActiveCamera active = bound.active();
        List<Entity> drawables = collectDrawableEntities(entities, layers);
        SpriteDrawOrder.sort(drawables, entities, active);

        batch.setProjectionMatrix(bound.combined());
        batch.begin();
        for (Entity entity : drawables) {
            drawSprite(entities, entity, active, bound);
        }
        batch.end();
    }

    private void drawSprite(EntityStore entities, Entity entity, ActiveCamera active, BoundCamera bound) {
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        Sprite sprite = entities.getComponent(entity.id(), Sprite.class);
        if (transform == null || sprite == null) {
            return;
        }
        String texturePath = sprite.texture();
        if (texturePath == null || texturePath.isBlank()) {
            return;
        }
        ResourceRef ref = ResourceRef.of(texturePath);
        resources.loadSync(ref, ResourceKind.TEXTURE);
        TextureRegion region = ResourceAccess.textureRegion(resources, ref);

        ShaderProgram previousShader = batch.getShader();
        Material material = entities.getComponent(entity.id(), Material.class);
        ShaderProgram materialShader = resolveSpriteShader(material);
        if (materialShader != null) {
            batch.setShader(materialShader);
            MaterialUniformBinder.apply(materialShader, material);
        }

        float width = region.getRegionWidth() * transform.scaleX();
        float height = region.getRegionHeight() * transform.scaleY();
        float originX = width * 0.5f;
        float originY = height * 0.5f;
        float x = transform.x();
        float y = transform.y();
        float rotation = transform.rotationZ();

        try {
            if (active.projection() == Camera.Projection.PERSPECTIVE) {
                projected.set(x, y, transform.z());
                bound.gdxCamera().project(projected);
                batch.draw(
                        region, projected.x, projected.y, originX, originY, width, height, 1f, 1f, rotation);
            } else {
                batch.draw(region, x, y, originX, originY, width, height, 1f, 1f, rotation);
            }
        } finally {
            if (materialShader != null) {
                batch.setShader(previousShader);
            }
        }
    }

    private ShaderProgram resolveSpriteShader(Material material) {
        if (shaderRegistry == null || material == null) {
            return null;
        }
        String shaderId = material.shader();
        if (!shaderRegistry.isRegistered(shaderId)) {
            throw new ShaderCompileException("shader not registered: " + shaderId);
        }
        if (shaderRegistry.usesBuiltin(shaderId)) {
            return null;
        }
        if (!shaderRegistry.supportsSpriteBatch(shaderId)) {
            return null;
        }
        return shaderRegistry.requireProgram(shaderId);
    }

    ShaderProgram resolveSpriteShaderForTest(Material material) {
        return resolveSpriteShader(material);
    }

    private static List<Entity> collectDrawableEntities(EntityStore entities, Set<RenderLayer.Layer> layers) {
        Set<RenderLayer.Layer> allowed =
                layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.WORLD) : layers;
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities.entitiesWith(Sprite.class)) {
            if (entities.hasComponent(entity.id(), Camera.class)) {
                continue;
            }
            if (!entities.hasComponent(entity.id(), Transform.class)) {
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

    public void dispose() {
    }
}
