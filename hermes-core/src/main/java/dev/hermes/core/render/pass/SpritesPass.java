package dev.hermes.core.render.pass;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.DrawableKind;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.SpriteDrawOrder;
import dev.hermes.core.render.ShaderCompileException;
import dev.hermes.core.render.TransformComposer;
import dev.hermes.core.render.resource.MaterialUniformBinder;
import dev.hermes.core.render.resource.ShaderRegistry;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.resource.loaders.SpriteSheetResourceLoader;
import dev.hermes.core.world.tilemap.TileMapRenderSystem;
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
    private final TileMapRenderSystem tileMaps;
    private final Vector3 projected = new Vector3();
    private final Matrix4 composedTransform = new Matrix4();

    public SpritesPass(SpriteBatch batch, ResourceManagerImpl resources) {
        this(batch, null, resources);
    }

    public SpritesPass(SpriteBatch batch, ShaderRegistry shaderRegistry, ResourceManagerImpl resources) {
        this.batch = batch;
        this.shaderRegistry = shaderRegistry;
        this.resources = Objects.requireNonNull(resources, "resources");
        this.tileMaps = new TileMapRenderSystem(resources);
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
        tileMaps.render(entities, layers, bound, batch);
        for (Entity entity : drawables) {
            drawSprite(entities, entity, active, bound);
        }
        batch.end();
    }

    private void drawSprite(EntityStore entities, Entity entity, ActiveCamera active, BoundCamera bound) {
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        Drawables drawables = entities.getComponent(entity.id(), Drawables.class);
        if (transform == null || drawables == null) {
            return;
        }

        Material material = entities.getComponent(entity.id(), Material.class);
        ShaderProgram previousShader = batch.getShader();
        ShaderProgram materialShader = resolveSpriteShader(material);
        if (materialShader != null) {
            batch.setShader(materialShader);
            MaterialUniformBinder.apply(materialShader, material);
        }

        try {
            for (DrawablePart part : drawables.parts()) {
                if (!shouldDrawSpritePart(part)) {
                    continue;
                }
                drawSpritePart(transform, part, active, bound);
            }
        } finally {
            if (materialShader != null) {
                batch.setShader(previousShader);
            }
        }
    }

    private void drawSpritePart(
            Transform transform, DrawablePart part, ActiveCamera active, BoundCamera bound) {
        String texturePath = part.texture();
        ResourceRef textureRef = ResourceRef.of(texturePath);
        TextureRegion region = resolveSpriteRegion(part, texturePath, textureRef);

        TransformComposer.composeInto(composedTransform, transform, part.local());
        float x = composedTransform.val[Matrix4.M03];
        float y = composedTransform.val[Matrix4.M13];
        float z = composedTransform.val[Matrix4.M23];
        float rotation = spriteRotationZ(composedTransform);
        float scaleX = composedTransform.getScaleX();
        float scaleY = composedTransform.getScaleY();

        float width = region.getRegionWidth() * scaleX;
        float height = region.getRegionHeight() * scaleY;
        float originX = width * 0.5f;
        float originY = height * 0.5f;

        if (active.projection() == Camera.Projection.PERSPECTIVE) {
            projected.set(x, y, z);
            bound.gdxCamera().project(projected);
            batch.draw(
                    region, projected.x, projected.y, originX, originY, width, height, 1f, 1f, rotation);
        } else {
            batch.draw(region, x, y, originX, originY, width, height, 1f, 1f, rotation);
        }
    }

    private TextureRegion resolveSpriteRegion(DrawablePart part, String texturePath, ResourceRef textureRef) {
        if (part.sheet() != null) {
            ResourceRef sheetRef = SpriteSheetResourceLoader.ref(texturePath, part.sheet());
            resources.loadSync(textureRef, ResourceKind.TEXTURE);
            resources.loadSync(sheetRef, ResourceKind.SPRITE_SHEET);
            TextureRegion[] frames = ResourceAccess.spriteSheetFrames(resources, sheetRef);
            int frameIndex = part.local().spriteFrame();
            if (frameIndex < 0 || frameIndex >= frames.length) {
                frameIndex = 0;
            }
            return frames[frameIndex];
        }
        resources.loadSync(textureRef, ResourceKind.TEXTURE);
        return ResourceAccess.textureRegion(resources, textureRef);
    }

    static boolean shouldDrawSpritePart(DrawablePart part) {
        if (!part.local().visible() || part.kind() != DrawableKind.SPRITE) {
            return false;
        }
        String texturePath = part.texture();
        return texturePath != null && !texturePath.isBlank();
    }

    private static float spriteRotationZ(Matrix4 matrix) {
        return (float) Math.toDegrees(Math.atan2(matrix.val[Matrix4.M10], matrix.val[Matrix4.M00]));
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
        for (Entity entity : entities.entitiesWith(Drawables.class)) {
            if (entities.hasComponent(entity.id(), Camera.class)) {
                continue;
            }
            if (!entities.hasComponent(entity.id(), Transform.class)) {
                continue;
            }
            Drawables drawables = entities.getComponent(entity.id(), Drawables.class);
            if (!hasVisibleSpritePart(drawables)) {
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

    private static boolean hasVisibleSpritePart(Drawables drawables) {
        if (drawables == null) {
            return false;
        }
        for (DrawablePart part : drawables.parts()) {
            if (shouldDrawSpritePart(part)) {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
    }
}
