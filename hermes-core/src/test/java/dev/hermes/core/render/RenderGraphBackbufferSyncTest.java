package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.core.TestGdx;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

final class RenderGraphBackbufferSyncTest {

    @Test
    void render_syncsBackbufferWhenGdxGraphicsSizeChanges() throws Exception {
        TestGdx.initHeadlessGl();

        ResizableMockGraphics graphics = new ResizableMockGraphics();
        graphics.setBackbuffer(1, 1);
        Gdx.graphics = graphics;

        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"world3d\", \"type\": \"world3d\", \"target\": \"screen\", \"layers\": [\"WORLD\"] }\n"
                        + "  ]\n"
                        + "}\n";
        RenderGraph graph = new RenderGraphBuilder().buildWithStubs(PipelineDocument.parse(json));
        graph.resize(1, 1);

        graphics.setBackbuffer(1280, 720);
        graph.render(null);

        FramebufferPool pool = graph.framebufferPool();
        assertEquals(1280, poolWindowWidth(pool));
        assertEquals(720, poolWindowHeight(pool));
        graph.dispose();
    }

    @Test
    void render_syncsToBackbufferWhenLogicalSizeDiffers() throws Exception {
        TestGdx.initHeadlessGl();

        RetinaMockGraphics graphics = new RetinaMockGraphics(640, 480, 1280, 960);
        Gdx.graphics = graphics;

        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"world3d\", \"type\": \"world3d\", \"target\": \"screen\", \"layers\": [\"WORLD\"] }\n"
                        + "  ]\n"
                        + "}\n";
        RenderGraph graph = new RenderGraphBuilder().buildWithStubs(PipelineDocument.parse(json));
        graph.resize(640, 480);

        graph.render(null);

        FramebufferPool pool = graph.framebufferPool();
        assertEquals(1280, poolWindowWidth(pool));
        assertEquals(960, poolWindowHeight(pool));
        graph.dispose();
    }

    private static int poolWindowWidth(FramebufferPool pool) throws Exception {
        Field field = FramebufferPool.class.getDeclaredField("windowWidth");
        field.setAccessible(true);
        return field.getInt(pool);
    }

    private static int poolWindowHeight(FramebufferPool pool) throws Exception {
        Field field = FramebufferPool.class.getDeclaredField("windowHeight");
        field.setAccessible(true);
        return field.getInt(pool);
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private int width = 480;
        private int height = 320;

        void setBackbuffer(int width, int height) {
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
