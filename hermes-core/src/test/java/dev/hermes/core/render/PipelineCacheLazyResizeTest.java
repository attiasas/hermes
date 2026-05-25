package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.core.TestGdx;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

final class PipelineCacheLazyResizeTest {

    @Test
    void lazyCreatedGraph_matchesCurrentBackbufferSize() throws Exception {
        TestGdx.initHeadlessGl();
        ResizableMockGraphics graphics = new ResizableMockGraphics();
        graphics.setBackbuffer(1280, 720);
        Gdx.graphics = graphics;

        ViewportServiceImpl viewport = new ViewportServiceImpl();
        PipelineCache cache = new PipelineCache(new RenderPassRegistry(), viewport);
        cache.resize(1280, 720);

        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"world3d\", \"type\": \"world3d\", \"target\": \"screen\", \"layers\": [\"WORLD\"] }\n"
                        + "  ]\n"
                        + "}\n";
        RenderGraph graph = cache.getForTest(PipelineDocument.parse(json));
        FramebufferPool pool = graph.framebufferPool();

        assertEquals(1280, poolWindowWidth(pool));
        assertEquals(720, poolWindowHeight(pool));
        graph.dispose();
        cache.dispose();
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
}
