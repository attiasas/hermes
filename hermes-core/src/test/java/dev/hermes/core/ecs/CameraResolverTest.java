package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
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
    void usesFirstActiveCamera() {
        WorldImpl world = new WorldImpl();

        Entity inactive = world.createEntity("cam-inactive");
        Camera inactiveCamera = new Camera();
        inactiveCamera.setActive(false);
        world.addComponent(inactive.id(), inactiveCamera);
        world.addComponent(inactive.id(), new Transform(0f, 0f));

        Entity activeEntity = world.createEntity("cam-active");
        Camera activeCamera = new Camera();
        activeCamera.setActive(true);
        activeCamera.setZoom(2f);
        world.addComponent(activeEntity.id(), activeCamera);
        Transform activeTransform = new Transform(100f, 200f, 5f);
        world.addComponent(activeEntity.id(), activeTransform);

        ActiveCamera resolved = CameraResolver.resolve(world, 640f, 480f);

        assertEquals(Camera.Projection.ORTHOGRAPHIC, resolved.projection());
        assertEquals(100f, resolved.x());
        assertEquals(200f, resolved.y());
        assertEquals(5f, resolved.z());
        assertEquals(2f, resolved.zoom());
    }

    @Test
    void fallsBackToFirstCameraWhenNoneActive() {
        WorldImpl world = new WorldImpl();
        Entity cam = world.createEntity("cam");
        Camera camera = new Camera();
        camera.setActive(false);
        camera.setProjection(Camera.Projection.PERSPECTIVE);
        camera.setFieldOfView(45f);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(1f, 2f, 3f));

        ActiveCamera active = CameraResolver.resolve(world, 800f, 600f);

        assertEquals(Camera.Projection.PERSPECTIVE, active.projection());
        assertEquals(45f, active.fieldOfView());
        assertEquals(1f, active.x());
    }

    @Test
    void activeCameraEntity_returnsFirstActiveWithTransform() {
        WorldImpl world = new WorldImpl();
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        SceneLoader.load("scenes/camera-pick-test.json", world, registry);

        Optional<Entity> cam = CameraResolver.activeCameraEntity(world);

        assertTrue(cam.isPresent());
        assertEquals("main-camera", cam.get().name());
    }

    @Test
    void defaultCameraWhenNoCameraEntities() {
        WorldImpl world = new WorldImpl();
        ActiveCamera active = CameraResolver.resolve(world, 640f, 480f);

        assertEquals(Camera.Projection.ORTHOGRAPHIC, active.projection());
        assertEquals(320f, active.x());
        assertEquals(240f, active.y());
        assertEquals(1f, active.zoom());
        assertEquals(640f, active.viewportWidth());
        assertEquals(480f, active.viewportHeight());
    }
}
