package dev.hermes.core.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.core.FramebufferGlMock;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadingScreenControllerTest {

    private LoadingScreenController controller;
    private SpriteBatch batch;

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
        controller = new LoadingScreenController();
        batch = newBatch();
    }

    private SpriteBatch newBatch() {
        return new SpriteBatch();
    }

    @AfterEach
    void tearDown() {
        if (batch != null) {
            batch.dispose();
        }
        if (controller != null) {
            controller.dispose();
        }
    }

    @Test
    void beginMakesOverlayVisible() {
        assertFalse(controller.isVisible());
        controller.begin(() -> 0f, "Loading");
        assertTrue(controller.isVisible());
    }

    @Test
    void endHidesOverlay() {
        controller.begin(() -> 0.5f, "Loading");
        assertTrue(controller.isVisible());
        controller.end();
        assertFalse(controller.isVisible());
    }

    @Test
    void renderQueriesProgressSupplier() {
        AtomicInteger calls = new AtomicInteger();
        controller.begin(() -> {
            calls.incrementAndGet();
            return 0.42f;
        }, "main-menu");
        controller.render(batch, 640, 360);
        assertTrue(calls.get() > 0, "progress supplier should be called during render");
    }

    @Test
    void renderDoesNothingWhenHidden() {
        AtomicBoolean called = new AtomicBoolean();
        controller.begin(() -> {
            called.set(true);
            return 1f;
        }, "hidden");
        controller.end();
        controller.render(batch, 640, 360);
        assertFalse(called.get());
    }
}
