package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import org.junit.jupiter.api.Test;

import dev.hermes.core.ecs.WorldManagerImpl;

final class WorldBlockParserTest {

    @Test
    void parsesExplicitDimensions() {
        String json =
                "{ \"world\": { \"version\": 1, \"dimensions\": { \"width\": 800, \"height\": 600 } },"
                        + " \"entities\": [] }";
        JsonValue root = new JsonReader().parse(json);
        WorldBlock block = WorldBlockParser.parse("test.json", root.get("world"));
        assertEquals(800f, block.bounds().maxX(), 0.001f);
        assertEquals(600f, block.bounds().maxY(), 0.001f);
    }

    @Test
    void applyWorldBlockOnSceneLoad() {
        String json =
                "{ \"world\": { \"version\": 1, \"dimensions\": { \"width\": 400, \"height\": 300 } },"
                        + " \"entities\": [] }";
        WorldManagerImpl manager = new WorldManagerImpl();
        dev.hermes.core.ecs.SceneLoader.loadFromString(
                "test.json", json, manager, new dev.hermes.core.ecs.ComponentRegistryImpl());
        assertEquals(400f, manager.space().bounds().maxX(), 0.001f);
        assertEquals(300f, manager.space().bounds().maxY(), 0.001f);
    }
}
