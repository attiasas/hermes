package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ecs.Camera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class CameraParserTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void loadsOrthographicCamera() {
        String json =
                "{\n"
                        + "  \"entities\": [\n"
                        + "    {\n"
                        + "      \"id\": \"cam\",\n"
                        + "      \"components\": {\n"
                        + "        \"Transform\": { \"x\": 10, \"y\": 20, \"z\": 0 },\n"
                        + "        \"Camera\": { \"projection\": \"orthographic\", \"zoom\": 2, \"active\": true }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        WorldImpl world = new WorldImpl();
        SceneLoader.loadFromString("scenes/test.json", json, world, registry);

        Camera camera = world.getComponent(world.findByName("cam").id(), Camera.class);
        assertEquals(Camera.Projection.ORTHOGRAPHIC, camera.projection());
        assertEquals(2f, camera.zoom());
        assertTrue(camera.active());
    }

    @Test
    void loadsRenderTargetWhenPresent() {
        String json =
                "{\n"
                        + "  \"entities\": [\n"
                        + "    {\n"
                        + "      \"id\": \"cam\",\n"
                        + "      \"components\": {\n"
                        + "        \"Transform\": { \"x\": 0, \"y\": 0, \"z\": 0 },\n"
                        + "        \"Camera\": { \"renderTarget\": \"sceneColor\" }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        WorldImpl world = new WorldImpl();
        SceneLoader.loadFromString("scenes/test.json", json, world, registry);

        Camera camera = world.getComponent(world.findByName("cam").id(), Camera.class);
        assertEquals("sceneColor", camera.renderTarget());
    }

    @Test
    void loadsPerspectiveCamera() {
        String json =
                "{\n"
                        + "  \"entities\": [\n"
                        + "    {\n"
                        + "      \"id\": \"cam\",\n"
                        + "      \"components\": {\n"
                        + "        \"Transform\": { \"x\": 0, \"y\": 0, \"z\": 50 },\n"
                        + "        \"Camera\": {\n"
                        + "          \"projection\": \"perspective\",\n"
                        + "          \"fieldOfView\": 90,\n"
                        + "          \"near\": 1,\n"
                        + "          \"far\": 500,\n"
                        + "          \"active\": false\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        WorldImpl world = new WorldImpl();
        SceneLoader.loadFromString("scenes/test.json", json, world, registry);

        Camera camera = world.getComponent(world.findByName("cam").id(), Camera.class);
        assertEquals(Camera.Projection.PERSPECTIVE, camera.projection());
        assertEquals(90f, camera.fieldOfView());
        assertEquals(1f, camera.near());
        assertEquals(500f, camera.far());
        assertFalse(camera.active());
    }
}
