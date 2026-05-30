package dev.hermes.core.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.math.Rect4;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.EntityStoreImpl;
import dev.hermes.api.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class SceneViewportImplTest {

    @BeforeAll
    static void init() {
        TestGdx.initHeadlessGl();
    }

    @Test
    void binderWiresSurfaceAndCamera() {
        EntityStoreImpl world = new EntityStoreImpl();
        Entity cam = world.create("cam");
        Camera camera = new Camera();
        camera.setFitMode(ViewportFitMode.STRETCH);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(320f, 240f, 0f));

        ViewportCameraBinder binder = new ViewportCameraBinder();
        Rect4 rect = ViewportLayout.stretch(640, 480);
        RenderSurface surface = RenderSurface.screen(640, 480, rect);
        ActiveCamera active = CameraResolver.resolveForPass(world, "screen", 640f, 480f);
        BoundCamera bound = binder.bind(active, surface);
        SceneViewportImpl vp = new SceneViewportImpl(bound);

        assertNotNull(vp);
        assertNotNull(bound.gdxCamera());
        assertEquals(640, surface.pixelWidth());
        assertEquals(480, surface.pixelHeight());
        assertEquals(320f, bound.active().x(), 0.01f);
    }
}
