package dev.hermes.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.render.RenderPipelineExecutor;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class RenderPipelineViewportResetTest {

    private static FramebufferGlMock.RecordingGl gl;
    private RenderPipelineExecutor executor;

    @BeforeAll
    static void initGl() {
        TestGdx.initHeadlessGl();
        TestGdx.initClasspathFiles();
        gl = FramebufferGlMock.create();
        Gdx.gl20 = gl.gl();
        Gdx.gl = Gdx.gl20;
        Gdx.graphics = new ResizableMockGraphics(800, 600);
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
        executor.resize(800, 600);
    }

    @AfterEach
    void tearDown() {
        executor.dispose();
        gl.clearViewportCalls();
    }

    @Test
    void execute_resetsViewportToFullBackbufferBeforeClear() {
        Gdx.gl.glViewport(100, 50, 600, 500);

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
        gl.clearViewportCalls();

        executor.execute(List.of(scene));

        assertTrue(
                gl.viewportCalls().stream()
                        .anyMatch(
                                c ->
                                        c[0] == 0
                                                && c[1] == 0
                                                && c[2] == 800
                                                && c[3] == 600),
                "first glViewport each frame must be full backbuffer before clear");
    }

    @Test
    void resize_resetsViewportToNewBackbufferSize() {
        executor.resize(1024, 768);
        gl.clearViewportCalls();
        executor.resize(640, 480);
        assertTrue(
                gl.viewportCalls().stream()
                        .anyMatch(c -> c[0] == 0 && c[1] == 0 && c[2] == 640 && c[3] == 480),
                "resize must apply full backbuffer viewport immediately");
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private int width;
        private int height;

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
