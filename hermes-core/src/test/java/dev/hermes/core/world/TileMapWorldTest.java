package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.world.WorldKind;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.world.tilemap.HermesTileMapLoader;
import dev.hermes.core.world.tilemap.TileMapAsset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class TileMapWorldTest {

    private static final String MAP_PATH = "maps/test.hmap.json";

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void loadsTilemapAssetWithExpectedDimensions() {
        HermesTileMapLoader loader = new HermesTileMapLoader();
        TileMapAsset asset = (TileMapAsset) loader.upload(loader.decode(MAP_PATH));
        assertEquals(40, asset.width());
        assertEquals(30, asset.height());
        assertEquals(32, asset.tileWidth());
        assertEquals(32, asset.tileHeight());
        assertEquals(1280f, asset.worldWidth(), 0.001f);
        assertEquals(960f, asset.worldHeight(), 0.001f);
    }

    @Test
    void tilemapWorldBoundsAndSpatialQuery() {
        WorldManagerImpl manager = new WorldManagerImpl();
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        String json =
                "{ \"world\": { \"version\": 1, \"kind\": \"tilemap\", \"tilemap\": \""
                        + MAP_PATH
                        + "\" },"
                        + " \"entities\": ["
                        + " { \"id\": \"player\", \"components\": { \"Transform\": { \"x\": 176, \"y\": 176 } } }"
                        + " ] }";
        dev.hermes.core.ecs.SceneLoader.loadFromString(
                "tilemap-test.json", json, manager, registry);

        assertEquals(WorldKind.TILEMAP, manager.space().kind());
        assertEquals(1280f, manager.space().bounds().maxX(), 0.001f);
        assertEquals(960f, manager.space().bounds().maxY(), 0.001f);

        manager.space().spatial().rebuild(manager.entities());
        float tileCenterX = 5 * 32f + 16f;
        float tileCenterY = 5 * 32f + 16f;
        assertTrue(manager.space().queryNear(tileCenterX, tileCenterY, 1f).size() >= 1);
    }
}
