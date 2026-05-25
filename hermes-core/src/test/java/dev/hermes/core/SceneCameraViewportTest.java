package dev.hermes.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.math.Rect4;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.SceneCamera;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class SceneCameraViewportTest {

    private static FramebufferGlMock.RecordingGl recordingGl;

    @BeforeAll
    static void initGl() {
        TestGdx.initHeadlessGl();
        recordingGl = FramebufferGlMock.create();
        Gdx.gl20 = recordingGl.gl();
        Gdx.gl = Gdx.gl20;
    }

    @Test
    void applyGlViewportUsesLetterboxRect() {
        recordingGl.clearViewportCalls();
        SceneCamera camera = new SceneCamera();
        ActiveCamera active =
                new ActiveCamera(
                        Camera.Projection.ORTHOGRAPHIC,
                        400f,
                        300f,
                        0f,
                        0f,
                        0f,
                        0f,
                        1f,
                        67f,
                        0.1f,
                        3000f,
                        800f,
                        600f,
                        ViewportFitMode.LETTERBOX,
                        16f / 9f,
                        Float.NaN,
                        Float.NaN,
                        Float.NaN,
                        null);
        Rect4 rect = new Rect4().set(0f, 75f, 800f, 450f);
        camera.apply(active, 800, 600, rect);
        camera.applyGlViewport(rect);

        assertEquals(1, recordingGl.viewportCalls().size());
        int[] vp = recordingGl.viewportCalls().get(0);
        assertEquals(0, vp[0]);
        assertEquals(75, vp[1]);
        assertEquals(800, vp[2]);
        assertEquals(450, vp[3]);
    }

    @Test
    void perspectiveLookAtFacesOrigin() {
        SceneCamera camera = new SceneCamera();
        ActiveCamera active =
                new ActiveCamera(
                        Camera.Projection.PERSPECTIVE,
                        0f,
                        2f,
                        6f,
                        0f,
                        0f,
                        0f,
                        1f,
                        60f,
                        0.1f,
                        100f,
                        640f,
                        480f,
                        ViewportFitMode.STRETCH,
                        0f,
                        0f,
                        0f,
                        0f,
                        null);
        camera.apply(active, 640, 480, new Rect4().set(0, 0, 640, 480));
        float dx = camera.gdxCamera().direction.x;
        float dy = camera.gdxCamera().direction.y;
        float dz = camera.gdxCamera().direction.z;
        assertEquals(0f, dx, 0.05f);
        assertEquals(-0.33f, dy, 0.1f);
        assertEquals(-0.94f, dz, 0.1f);
    }
}
