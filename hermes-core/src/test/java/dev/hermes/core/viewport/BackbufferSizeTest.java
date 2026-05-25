package dev.hermes.core.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import dev.hermes.core.TestGdx;

import org.junit.jupiter.api.Test;

final class BackbufferSizeTest {

    @Test
    void widthAndHeight_useBackBufferDimensionsWithFallback() {
        TestGdx.initHeadlessGl();
        ResizableMockGraphics graphics = new ResizableMockGraphics();
        graphics.setBackbuffer(1920, 1080);
        Gdx.graphics = graphics;

        assertEquals(1920, BackbufferSize.width());
        assertEquals(1080, BackbufferSize.height());
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private int width = 1;
        private int height = 1;

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
