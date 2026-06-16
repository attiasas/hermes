package dev.hermes.core.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.EntityStoreImpl;
import org.junit.jupiter.api.Test;

final class RenderSurfaceCameraTest {

    @Test
    void resolveUsesFramebufferSurfaceNotWindow() {
        EntityStoreImpl world = new EntityStoreImpl();
        Entity cam = world.create("cam");
        Camera camera = new Camera();
        camera.setProjection(Camera.Projection.PERSPECTIVE);
        camera.setRenderTarget("sceneColor");
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(0f, 0f, 5f));

        RenderSurface surface =
                RenderSurface.framebuffer(
                        "sceneColor", 400, 300, ViewportLayout.stretch(400, 300));

        ActiveCamera active = CameraResolver.resolveForPass(world, "sceneColor", 400f, 300f);

        assertEquals(400f, active.viewportWidth());
        assertEquals(300f, active.viewportHeight());
    }

    @Test
    void resolvePrefersCameraWithMatchingRenderTarget() {
        EntityStoreImpl world = new EntityStoreImpl();
        Entity worldCam = world.create("world-cam");
        Camera c1 = new Camera();
        c1.setRenderTarget("sceneColor");
        world.addComponent(worldCam.id(), c1);
        world.addComponent(worldCam.id(), new Transform(0, 0, 5));

        Entity screenCam = world.create("screen-cam");
        Camera c2 = new Camera();
        world.addComponent(screenCam.id(), c2);
        world.addComponent(screenCam.id(), new Transform(0, 0, 10));

        ActiveCamera active = CameraResolver.resolveForPass(world, "sceneColor", 400f, 300f);
        assertEquals(5f, active.z());
    }
}
