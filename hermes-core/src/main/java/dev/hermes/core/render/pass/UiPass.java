package dev.hermes.core.render.pass;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.Sprite;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.World;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.SceneCamera;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders UI-layer sprites using an optional named scene camera or screen-space ortho.
 */
public final class UiPass {

    private final SpriteBatch batch;
    private final Map<String, TextureRegion> regions = new HashMap<>();
    private final SceneCamera sceneCamera = new SceneCamera();
    private final String cameraEntityName;
    private float windowWidth = 640f;
    private float windowHeight = 480f;

    public UiPass(SpriteBatch batch) {
        this(batch, null);
    }

    public UiPass(SpriteBatch batch, String cameraEntityName) {
        this.batch = batch;
        this.cameraEntityName = cameraEntityName;
        sceneCamera.resize(windowWidth, windowHeight);
    }

    public void resize(int width, int height) {
        windowWidth = Math.max(1, width);
        windowHeight = Math.max(1, height);
        sceneCamera.resize(windowWidth, windowHeight);
    }

    public void render(World world, Set<RenderLayer.Layer> layers) {
        List<Entity> drawables = collectDrawableEntities(world, layers);
        if (drawables.isEmpty()) {
            return;
        }
        drawables.sort(
                Comparator.comparingDouble(e -> world.getComponent(e.id(), Transform.class).z()));

        ActiveCamera active =
                CameraResolver.resolveNamed(
                        world, cameraEntityName, "screen", windowWidth, windowHeight);
        sceneCamera.apply(active);

        batch.setProjectionMatrix(sceneCamera.combined());
        batch.begin();
        for (Entity entity : drawables) {
            drawSprite(world, entity);
        }
        batch.end();
    }

    private void drawSprite(World world, Entity entity) {
        Transform transform = world.getComponent(entity.id(), Transform.class);
        Sprite sprite = world.getComponent(entity.id(), Sprite.class);
        if (transform == null || sprite == null) {
            return;
        }
        String texturePath = sprite.texture();
        if (texturePath == null || texturePath.isBlank()) {
            return;
        }
        TextureRegion region =
                regions.computeIfAbsent(
                        texturePath,
                        path -> {
                            FileHandle file = HermesAssetPaths.internal(path);
                            return new TextureRegion(new Texture(file));
                        });
        float width = region.getRegionWidth() * transform.scaleX();
        float height = region.getRegionHeight() * transform.scaleY();
        float originX = width * 0.5f;
        float originY = height * 0.5f;
        batch.draw(
                region,
                transform.x(),
                transform.y(),
                originX,
                originY,
                width,
                height,
                1f,
                1f,
                transform.rotationZ());
    }

    private static List<Entity> collectDrawableEntities(World world, Set<RenderLayer.Layer> layers) {
        Set<RenderLayer.Layer> allowed =
                layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.UI) : layers;
        List<Entity> result = new ArrayList<>();
        for (Entity entity : world.entitiesWith(Sprite.class)) {
            if (world.hasComponent(entity.id(), Camera.class)) {
                continue;
            }
            if (!world.hasComponent(entity.id(), Transform.class)) {
                continue;
            }
            RenderLayer renderLayer = world.getComponent(entity.id(), RenderLayer.class);
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
        for (TextureRegion region : regions.values()) {
            region.getTexture().dispose();
        }
        regions.clear();
    }
}
