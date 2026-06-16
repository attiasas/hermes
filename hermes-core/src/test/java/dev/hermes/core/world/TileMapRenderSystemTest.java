package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.TileMap;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.math.Rect4;
import dev.hermes.core.FramebufferGlMock;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;
import dev.hermes.core.viewport.ViewportCameraBinder;
import dev.hermes.core.world.tilemap.TileMapRenderSystem;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TileMapRenderSystemTest {

    private ResourceManagerImpl resources;
    private TileMapRenderSystem system;
    private DrawCountingBatch batch;

    @BeforeAll
    static void initGl() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        FramebufferGlMock.RecordingGl gl = FramebufferGlMock.create();
        Gdx.gl20 = gl.gl();
        Gdx.gl = Gdx.gl20;
    }

    @BeforeEach
    void setUp() {
        resources = ResourceManagerImpl.createDefault();
        system = new TileMapRenderSystem(resources);
        batch = new DrawCountingBatch();
    }

    @Test
    void entityWithTileMapIssuesBatchDrawCalls() {
        WorldManagerImpl manager = new WorldManagerImpl();
        var ground = manager.entities().create("ground");
        manager.entities().addComponent(ground.id(), new TileMap("maps/render-tiles.hmap.json"));

        ActiveCamera active =
                new ActiveCamera(
                        Camera.Projection.ORTHOGRAPHIC,
                        64f,
                        64f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        67f,
                        0.1f,
                        3000f,
                        128f,
                        128f,
                        ViewportFitMode.STRETCH,
                        1f,
                        Float.NaN,
                        Float.NaN,
                        Float.NaN,
                        null);
        RenderSurface surface = RenderSurface.screen(128, 128, new Rect4().set(0f, 0f, 128f, 128f));
        BoundCamera bound = new ViewportCameraBinder().bind(active, surface);

        batch.setProjectionMatrix(bound.combined());
        batch.begin();
        system.render(manager.entities(), EnumSet.of(dev.hermes.api.ecs.RenderLayer.Layer.WORLD), bound, batch);
        batch.end();

        assertTrue(batch.draws > 0, "expected tile draw calls");
    }

    private static final class DrawCountingBatch extends SpriteBatch {
        int draws;

        @Override
        public void draw(TextureRegion region, float x, float y, float width, float height) {
            draws++;
            super.draw(region, x, y, width, height);
        }
    }
}
