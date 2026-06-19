package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.world.SceneCameraConfig;
import dev.hermes.core.TestGdx;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class CameraResolverTest {

    @BeforeAll
    static void initClasspath() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void screenPassUsesSceneCameraWhenNoBinding() {
        WorldManagerImpl manager = new WorldManagerImpl();
        SceneCameraConfig config = new SceneCameraConfig();
        config.setX(320f);
        config.setY(240f);
        manager.camera().setSceneConfig(config);
        ActiveCamera active = CameraResolver.resolveForManager(manager, "screen", 800f, 600f);
        assertEquals(320f, active.x(), 0.001f);
        assertEquals(240f, active.y(), 0.001f);
    }

    @Test
    void usesBoundEntityCameraForMainPass() {
        WorldManagerImpl manager = new WorldManagerImpl();
        var entity = manager.entities().create("player-cam");
        Camera camera = new Camera();
        camera.setZoom(2f);
        manager.entities().addComponent(entity.id(), camera);
        manager.entities().addComponent(entity.id(), new dev.hermes.api.ecs.Transform(100f, 200f, 5f));
        manager.camera().bindMain("player-cam");

        ActiveCamera resolved = CameraResolver.resolveForManager(manager, "screen", 640f, 480f);

        assertEquals(100f, resolved.x());
        assertEquals(200f, resolved.y());
        assertEquals(5f, resolved.z());
        assertEquals(2f, resolved.zoom());
    }

    @Test
    void mainCameraEntity_returnsBoundEntity() {
        WorldManagerImpl manager = new WorldManagerImpl();
        var entity = manager.entities().create("main-camera");
        manager.entities().addComponent(entity.id(), new Camera());
        manager.entities().addComponent(entity.id(), new dev.hermes.api.ecs.Transform(0f, 0f, 5f));
        manager.camera().bindMain("main-camera");

        Optional<dev.hermes.api.Entity> cam = CameraResolver.mainCameraEntity(manager);

        assertTrue(cam.isPresent());
        assertEquals("main-camera", cam.get().name());
    }

    @Test
    void defaultCameraWhenNoSceneConfigAndNoEntities() {
        WorldManagerImpl manager = new WorldManagerImpl();
        ActiveCamera active = CameraResolver.resolveForManager(manager, "screen", 640f, 480f);

        assertEquals(Camera.Projection.ORTHOGRAPHIC, active.projection());
        assertEquals(320f, active.x());
        assertEquals(240f, active.y());
        assertEquals(1f, active.zoom());
        assertEquals(640f, active.viewportWidth());
        assertEquals(480f, active.viewportHeight());
    }

    @Test
    void resolveRenderTargetCameraForFboPass() {
        WorldManagerImpl manager = new WorldManagerImpl();
        var mini = manager.entities().create("minimap-cam");
        Camera camera = new Camera();
        camera.setRenderTarget("minimap");
        camera.setZoom(0.1f);
        manager.entities().addComponent(mini.id(), camera);
        manager.entities().addComponent(mini.id(), new dev.hermes.api.ecs.Transform(1f, 2f, 3f));

        ActiveCamera active = CameraResolver.resolveForManager(manager, "minimap", 256f, 256f);

        assertEquals(0.1f, active.zoom(), 0.001f);
        assertEquals(1f, active.x(), 0.001f);
    }
}
