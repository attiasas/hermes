package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.UiAttach;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.RenderSurfaceDesc;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class UiAttachSystemTest {

    private WorldManagerImpl manager;
    private UiServiceImpl ui;
    private RecordingViewport viewport;
    private UiAttachSystem attachSystem;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        manager = new WorldManagerImpl(new EntityTypeRegistryImpl(), registry);
        ui = new UiServiceImpl(ResourceManagerImpl.createDefault());
        viewport = new RecordingViewport(400f, 300f);
        attachSystem = new UiAttachSystem(ui, viewport);
    }

    @Test
    void projectsFollowTargetAndLayoutsAttachNearScreenAnchor() {
        Entity player = manager.entities().create("player");
        manager.entities().addComponent(player.id(), new Transform(0f, 0f, 0f));

        Entity hp = manager.entities().create("hp");
        UiAttach attach = new UiAttach();
        attach.setDocument("ui/hp-bar.json");
        attach.setFollow("player");
        attach.setOffsetY(2f);
        manager.entities().addComponent(hp.id(), attach);

        attachSystem.update(manager, 0f);

        assertEquals(0f, viewport.lastWorldX, 0.001f);
        assertEquals(2f, viewport.lastWorldY, 0.001f);
        assertEquals(0f, viewport.lastWorldZ, 0.001f);

        UiLayoutResult layout =
                ui.layoutAttach(hp.id(), 800, 600).orElseThrow().layout();
        assertNotNull(layout);
        Rect4 root = layout.bounds("root");
        assertEquals(400f, root.x() + root.width() * 0.5f, 2f);
        assertEquals(300f, root.y(), 2f);
    }

    private static final class RecordingViewport implements ViewportService {

        private final float screenX;
        private final float screenY;
        float lastWorldX = Float.NaN;
        float lastWorldY = Float.NaN;
        float lastWorldZ = Float.NaN;

        RecordingViewport(float screenX, float screenY) {
            this.screenX = screenX;
            this.screenY = screenY;
        }

        @Override
        public void onWindowResize(int width, int height) {}

        @Override
        public int windowWidth() {
            return 800;
        }

        @Override
        public int windowHeight() {
            return 600;
        }

        @Override
        public RenderSurfaceDesc backbufferSurface(EntityStore entities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SceneViewport forWorld(EntityStore entities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SceneViewport forWorld(EntityStore entities, String cameraEntityName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SceneViewport forSurface(EntityStore entities, RenderSurfaceDesc surface) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void mapScreenToSurface(float screenX, float screenY, RenderSurfaceDesc surface, Vec2 out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void screenToWorld(EntityStore entities, float screenX, float screenY, float worldZ, Vec3 out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void worldToScreen(EntityStore entities, float worldX, float worldY, float worldZ, Vec2 out) {
            lastWorldX = worldX;
            lastWorldY = worldY;
            lastWorldZ = worldZ;
            out.set(screenX, screenY);
        }

        @Override
        public dev.hermes.api.math.ScreenRay screenRay(EntityStore entities, float screenX, float screenY) {
            throw new UnsupportedOperationException();
        }
    }
}
