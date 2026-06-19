package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import org.junit.jupiter.api.Test;

import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.core.ecs.WorldManagerImpl;

final class SceneCameraBlockParserTest {

    @Test
    void parsesPerspectiveCameraAndFollow() {
        String json =
                "{ \"camera\": { \"version\": 1, \"projection\": \"perspective\", \"z\": 5,"
                        + " \"fieldOfView\": 60, \"follow\": \"player\" }, \"entities\": [] }";
        JsonValue root = new JsonReader().parse(json);
        SceneCameraBlock block = SceneCameraBlockParser.parse("t.json", root.get("camera"));
        assertEquals(SceneCameraConfig.Projection.PERSPECTIVE, block.config().projection());
        assertEquals("player", block.followEntity().orElseThrow());
        assertEquals(5f, block.config().z(), 0.001f);
        assertEquals(60f, block.config().fieldOfView(), 0.001f);
    }

    @Test
    void applyCameraBlockOnSceneLoad() {
        String json =
                "{ \"camera\": { \"version\": 1, \"projection\": \"orthographic\", \"x\": 100, \"y\": 200,"
                        + " \"zoom\": 2 }, \"entities\": [] }";
        WorldManagerImpl manager = new WorldManagerImpl();
        dev.hermes.core.ecs.SceneLoader.loadFromString(
                "t.json", json, manager, new dev.hermes.core.ecs.ComponentRegistryImpl());
        assertEquals(100f, manager.camera().sceneConfig().x(), 0.001f);
        assertEquals(200f, manager.camera().sceneConfig().y(), 0.001f);
        assertEquals(2f, manager.camera().sceneConfig().zoom(), 0.001f);
    }
}
