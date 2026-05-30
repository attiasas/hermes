package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.core.FramebufferGlMock;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class RetinaBackbufferSyncTest {

    private static FramebufferGlMock.RecordingGl gl;
    private RenderPipelineExecutor executor;

    @BeforeAll
    static void initGl() {
        TestGdx.initHeadlessGl();
        TestGdx.initClasspathFiles();
        gl = FramebufferGlMock.create();
        Gdx.gl20 = gl.gl();
        Gdx.gl = Gdx.gl20;
        Gdx.graphics = new RetinaMockGraphics(640, 480, 1280, 960);
    }

    @BeforeEach
    void setUp() {
        gl.clearViewportCalls();
        executor =
                new RenderPipelineExecutor(
                        null,
                        "render/full-pipeline.json",
                        new RenderPassRegistry(),
                        new ViewportServiceImpl());
    }

    @AfterEach
    void tearDown() {
        executor.dispose();
        gl.clearViewportCalls();
    }

    @Test
    void execute_doesNotShrinkViewportBelowBackbufferOnRetina() {
        SceneHandle scene =
                new SceneHandle() {
                    @Override
                    public String id() {
                        return "main";
                    }

                    @Override
                    public dev.hermes.api.ecs.WorldManager manager() {
                        return new WorldManagerImpl();
                    }

                    @Override
                    public Optional<String> renderPipelineOverride() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<String> inputContext() {
                        return Optional.empty();
                    }

                    @Override
                    public boolean paused() {
                        return false;
                    }
                };

        executor.execute(List.of(scene));

        assertTrue(
                gl.viewportCalls().stream()
                        .anyMatch(c -> c[0] == 0 && c[1] == 0 && c[2] == 1280 && c[3] == 960),
                "full backbuffer clear must use physical pixels");
        assertFalse(
                gl.viewportCalls().stream()
                        .anyMatch(c -> c[2] == 640 && c[3] == 480),
                "must not shrink glViewport to logical size when backbuffer is 2x");
    }

    private static final class RetinaMockGraphics extends MockGraphics {
        private final int logicalW;
        private final int logicalH;
        private final int backbufferW;
        private final int backbufferH;

        RetinaMockGraphics(int logicalW, int logicalH, int backbufferW, int backbufferH) {
            this.logicalW = logicalW;
            this.logicalH = logicalH;
            this.backbufferW = backbufferW;
            this.backbufferH = backbufferH;
        }

        @Override
        public int getWidth() {
            return logicalW;
        }

        @Override
        public int getHeight() {
            return logicalH;
        }

        @Override
        public int getBackBufferWidth() {
            return backbufferW;
        }

        @Override
        public int getBackBufferHeight() {
            return backbufferH;
        }
    }
}
