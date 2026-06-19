package dev.hermes.core.world.tilemap;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.TileMap;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** Draws {@link TileMap} components using batched sprite quads. */
public final class TileMapRenderSystem {

    private final ResourceManagerImpl resources;
    private final float[] visibleRect = new float[4];
    private final Vector3 cornerA = new Vector3();
    private final Vector3 cornerB = new Vector3();

    public TileMapRenderSystem(ResourceManagerImpl resources) {
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    public void render(EntityStore entities, Set<RenderLayer.Layer> layers, BoundCamera bound, SpriteBatch batch) {
        ActiveCamera active = bound.active();
        if (active.projection() != Camera.Projection.ORTHOGRAPHIC) {
            return;
        }
        Set<RenderLayer.Layer> allowed =
                layers == null || layers.isEmpty() ? EnumSet.of(RenderLayer.Layer.WORLD) : layers;
        computeVisibleWorldRect(bound);
        for (Entity entity : entities.entitiesWith(TileMap.class)) {
            if (entities.hasComponent(entity.id(), Camera.class)) {
                continue;
            }
            RenderLayer renderLayer = entities.getComponent(entity.id(), RenderLayer.class);
            RenderLayer.Layer layer =
                    renderLayer == null ? RenderLayer.Layer.WORLD : renderLayer.layer();
            if (!allowed.contains(layer)) {
                continue;
            }
            drawEntityTileMap(entities, entity, batch);
        }
    }

    private void drawEntityTileMap(EntityStore entities, Entity entity, SpriteBatch batch) {
        TileMap tileMap = entities.getComponent(entity.id(), TileMap.class);
        if (tileMap == null) {
            return;
        }
        String mapPath = tileMap.map();
        if (mapPath == null || mapPath.isBlank()) {
            return;
        }
        ResourceRef mapRef = ResourceRef.of(mapPath);
        resources.loadSync(mapRef, ResourceKind.TILEMAP);
        TileMapAsset asset = ResourceAccess.tileMap(resources, mapRef);
        String layerName = tileMap.layer();
        int[] tiles = asset.layer(layerName).orElse(null);
        if (tiles == null) {
            return;
        }
        ResourceRef tilesetRef = ResourceRef.of(asset.tileset());
        resources.loadSync(tilesetRef, ResourceKind.TEXTURE);
        TextureRegion tileset = ResourceAccess.textureRegion(resources, tilesetRef);

        float offsetX = 0f;
        float offsetY = 0f;
        Transform transform = entities.getComponent(entity.id(), Transform.class);
        if (transform != null) {
            offsetX = transform.x();
            offsetY = transform.y();
        }

        int tileW = asset.tileWidth();
        int tileH = asset.tileHeight();
        int minTileX = tileIndex(visibleRect[0] - offsetX, tileW, asset.width());
        int minTileY = tileIndex(visibleRect[1] - offsetY, tileH, asset.height());
        int maxTileX = tileIndex(visibleRect[2] - offsetX, tileW, asset.width());
        int maxTileY = tileIndex(visibleRect[3] - offsetY, tileH, asset.height());

        for (int ty = minTileY; ty <= maxTileY; ty++) {
            for (int tx = minTileX; tx <= maxTileX; tx++) {
                int index = ty * asset.width() + tx;
                if (index < 0 || index >= tiles.length) {
                    continue;
                }
                int tileId = tiles[index];
                if (tileId <= 0) {
                    continue;
                }
                TextureRegion region = tileRegion(tileset, tileW, tileH, tileId);
                float worldX = offsetX + tx * (float) tileW;
                float worldY = offsetY + ty * (float) tileH;
                batch.draw(region, worldX, worldY, tileW, tileH);
            }
        }
    }

    private static TextureRegion tileRegion(TextureRegion tileset, int tileW, int tileH, int tileId) {
        int column = tileId - 1;
        TextureRegion region = new TextureRegion(tileset);
        region.setRegion(column * tileW, 0, tileW, tileH);
        return region;
    }

    private static int tileIndex(float worldCoord, int tileSize, int mapTiles) {
        if (tileSize <= 0) {
            return 0;
        }
        int index = (int) Math.floor(worldCoord / tileSize);
        if (index < 0) {
            return 0;
        }
        return Math.min(index, mapTiles - 1);
    }

    private void computeVisibleWorldRect(BoundCamera bound) {
        RenderSurface surface = bound.surface();
        Rect4 rect = surface.viewportRect();
        cornerA.set(rect.x, rect.y, 0f);
        cornerB.set(rect.x + rect.width, rect.y + rect.height, 0f);
        bound.gdxCamera().unproject(cornerA);
        bound.gdxCamera().unproject(cornerB);
        visibleRect[0] = Math.min(cornerA.x, cornerB.x);
        visibleRect[1] = Math.min(cornerA.y, cornerB.y);
        visibleRect[2] = Math.max(cornerA.x, cornerB.x);
        visibleRect[3] = Math.max(cornerA.y, cornerB.y);
    }
}
