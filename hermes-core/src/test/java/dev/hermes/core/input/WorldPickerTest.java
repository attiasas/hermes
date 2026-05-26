package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Selectable;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.input.PickHit;
import dev.hermes.api.input.PickLayer;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.ViewportService;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.WorldImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class WorldPickerTest {

    private ViewportServiceImpl viewport;
    private WorldPicker picker;
    private WorldImpl world;

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
    }

    @BeforeEach
    void setUp() {
        Gdx.graphics = new ResizableMockGraphics(640, 480);
        viewport = new ViewportServiceImpl();
        viewport.onWindowResize(640, 480);
        picker = new WorldPicker(viewport);
        world = new WorldImpl();
    }

    @Test
    void screenWorldRoundtrip_matchesOriginalWorldPoint() {
        Entity cam = world.createEntity("cam");
        Camera camera = new Camera();
        camera.setFitMode(ViewportFitMode.STRETCH);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(320f, 240f, 0f));

        Vec2 screen = new Vec2();
        viewport.forWorld(world).worldToScreen(100f, 200f, 0f, screen);
        Vec3 back = new Vec3();
        viewport.forWorld(world).screenToWorld(screen.x, screen.y, 0f, back);
        assertEquals(
                100f,
                back.x,
                1f,
                "screen=(" + screen.x + "," + screen.y + ") back=(" + back.x + "," + back.y + ")");
        assertEquals(
                200f,
                back.y,
                1f,
                "screen=(" + screen.x + "," + screen.y + ") back=(" + back.x + "," + back.y + ")");
    }

    @Test
    void orthoPick_hitsEntityAtScreenCoords() {
        Entity cam = world.createEntity("cam");
        Camera camera = new Camera();
        camera.setFitMode(ViewportFitMode.STRETCH);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(320f, 240f, 0f));

        Entity target = world.createEntity("target");
        world.addComponent(target.id(), new Transform(100f, 200f, 0f));
        Selectable selectable = new Selectable();
        selectable.setRadius(50f);
        world.addComponent(target.id(), selectable);

        Vec2 screen = new Vec2();
        viewport.forWorld(world).worldToScreen(100f, 200f, 0f, screen);

        Optional<PickHit> hit = picker.pick(world, screen.x, screen.y, PickLayer.WORLD);
        assertTrue(hit.isPresent());
        assertEquals(target.id(), hit.get().entity);
        assertEquals("target", hit.get().entityName);
        assertEquals(100f, hit.get().worldX, 0.01f);
        assertEquals(200f, hit.get().worldY, 0.01f);
    }

    @Test
    void orthoPick_missesOutsideRadius() {
        Entity cam = world.createEntity("cam");
        Camera camera = new Camera();
        camera.setFitMode(ViewportFitMode.STRETCH);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(320f, 240f, 0f));

        Entity target = world.createEntity("target");
        world.addComponent(target.id(), new Transform(100f, 200f, 0f));
        Selectable selectable = new Selectable();
        selectable.setRadius(10f);
        world.addComponent(target.id(), selectable);

        Vec2 screen = new Vec2();
        viewport.forWorld(world).worldToScreen(200f, 300f, 0f, screen);

        assertFalse(picker.pick(world, screen.x, screen.y, PickLayer.WORLD).isPresent());
    }

    @Test
    void orthoPick_closestEntityWins() {
        Entity cam = world.createEntity("cam");
        Camera camera = new Camera();
        camera.setFitMode(ViewportFitMode.STRETCH);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(320f, 240f, 0f));

        Entity near = world.createEntity("near");
        world.addComponent(near.id(), new Transform(150f, 240f, 0f));
        Selectable nearSel = new Selectable();
        nearSel.setRadius(80f);
        world.addComponent(near.id(), nearSel);

        Entity far = world.createEntity("far");
        world.addComponent(far.id(), new Transform(200f, 240f, 0f));
        Selectable farSel = new Selectable();
        farSel.setRadius(80f);
        world.addComponent(far.id(), farSel);

        Vec2 screen = new Vec2();
        viewport.forWorld(world).worldToScreen(160f, 240f, 0f, screen);

        Optional<PickHit> hit = picker.pick(world, screen.x, screen.y, PickLayer.WORLD);
        assertTrue(hit.isPresent());
        assertEquals(near.id(), hit.get().entity);
    }

    @Test
    void perspectivePick_hitsSphereAlongCameraForward() {
        Entity cam = world.createEntity("cam");
        Camera camera = new Camera();
        camera.setProjection(Camera.Projection.PERSPECTIVE);
        camera.setFitMode(ViewportFitMode.STRETCH);
        camera.setLookAt(0f, 0f, 0f);
        world.addComponent(cam.id(), camera);
        world.addComponent(cam.id(), new Transform(0f, 0f, 5f));

        Entity target = world.createEntity("cube");
        world.addComponent(target.id(), new Transform(0f, 0f, 0f));
        Selectable selectable = new Selectable();
        selectable.setRadius(1.5f);
        world.addComponent(target.id(), selectable);

        float screenX = viewport.windowWidth() * 0.5f;
        float screenY = viewport.windowHeight() * 0.5f;

        Optional<PickHit> hit = picker.pick(world, screenX, screenY, PickLayer.WORLD);
        assertTrue(hit.isPresent());
        assertEquals(target.id(), hit.get().entity);
        assertEquals("cube", hit.get().entityName);
        assertTrue(hit.get().distance > 0f);
    }

    @Test
    void raySphereHit_returnsSmallestPositiveT() {
        ScreenRay ray = new ScreenRay(0f, 0f, 5f, 0f, 0f, -1f);
        Optional<Float> t = WorldPicker.raySphereHit(ray, 0f, 0f, 0f, 1f);
        assertTrue(t.isPresent());
        assertEquals(4f, t.get(), 0.01f);
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private final int width;
        private final int height;

        ResizableMockGraphics(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getBackBufferWidth() {
            return width;
        }

        @Override
        public int getBackBufferHeight() {
            return height;
        }
    }
}
