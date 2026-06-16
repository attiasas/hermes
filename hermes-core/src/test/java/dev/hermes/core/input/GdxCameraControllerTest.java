package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.api.world.CameraControlsConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class GdxCameraControllerTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
    }

    @BeforeEach
    void resize() {
        Gdx.graphics = new ResizableMockGraphics();
    }

    @Test
    void orbit_increasesYawWhenDraggingRight() {
        ActiveCamera before =
                GdxCameraController.perspectiveAt(
                        0f, 2f, 5f, 0f, 0f, 0f, 0f, 0f, 0f, 640f, 480f);
        GdxCameraController ctrl = new GdxCameraController(640, 480);
        ActiveCamera after =
                ctrl.orbit(
                        before,
                        0f,
                        0f,
                        0f,
                        GdxCameraController.normalizeDeltaX(20f, 640),
                        0f,
                        CameraControlsConfig.orbitDefaults());
        assertNotEquals(before.rotationY(), after.rotationY(), 0.01f);
    }

    @Test
    void scrollZoom_movesCloserAlongView() {
        ActiveCamera before =
                GdxCameraController.perspectiveAt(
                        0f, 0f, 5f, 0f, 0f, 0f, 0f, 0f, 0f, 640f, 480f);
        GdxCameraController ctrl = new GdxCameraController(640, 480);
        ActiveCamera after =
                ctrl.scrollZoom(before, 0f, 0f, 0f, -1f, CameraControlsConfig.orbitDefaults());
        assertTrue(after.z() < before.z());
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        @Override
        public int getWidth() {
            return 640;
        }

        @Override
        public int getHeight() {
            return 480;
        }

        @Override
        public int getBackBufferWidth() {
            return 640;
        }

        @Override
        public int getBackBufferHeight() {
            return 480;
        }
    }
}
