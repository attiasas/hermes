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

        EntityStoreImpl world = new EntityStoreImpl();
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

        EntityStoreImpl world = new EntityStoreImpl();
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

        EntityStoreImpl world = new EntityStoreImpl();
        SceneLoader.loadFromString("scenes/test.json", json, world, registry);

        Camera camera = world.getComponent(world.findByName("cam").id(), Camera.class);
        assertEquals(Camera.Projection.PERSPECTIVE, camera.projection());
        assertEquals(90f, camera.fieldOfView());
        assertEquals(1f, camera.near());
        assertEquals(500f, camera.far());
        assertFalse(camera.active());
    }

    @Test
    void parsesFitModeAndLookAt() {
        String json =
                "{\n"
                        + "  \"entities\": [\n"
                        + "    {\n"
                        + "      \"id\": \"cam\",\n"
                        + "      \"components\": {\n"
                        + "        \"Transform\": { \"x\": 0, \"y\": 0, \"z\": 0 },\n"
                        + "        \"Camera\": {\n"
                        + "          \"projection\": \"perspective\",\n"
                        + "          \"fitMode\": \"letterbox\",\n"
                        + "          \"designAspect\": 1.7777778,\n"
                        + "          \"lookAt\": { \"x\": 1, \"y\": 2, \"z\": 3 }\n"
                        + "        }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        EntityStoreImpl world = new EntityStoreImpl();
        SceneLoader.loadFromString("scenes/test.json", json, world, registry);

        Camera c = world.getComponent(world.findByName("cam").id(), Camera.class);
        assertEquals(dev.hermes.api.ecs.ViewportFitMode.LETTERBOX, c.fitMode());
        assertEquals(1.7777778f, c.designAspect(), 0.0001f);
        assertEquals(1f, c.lookAtX(), 0.001f);
        assertEquals(2f, c.lookAtY(), 0.001f);
        assertEquals(3f, c.lookAtZ(), 0.001f);
    }
}
