package dev.hermes.core.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ecs.ViewportFitMode;
import dev.hermes.api.math.Rect4;
import org.junit.jupiter.api.Test;

final class ViewportLayoutTest {

    @Test
    void letterbox16x9OnSquareSurface() {
        Rect4 rect = new Rect4();
        ViewportLayout.compute(1000, 1000, 16f / 9f, ViewportFitMode.LETTERBOX, rect);
        assertEquals(1000f, rect.width);
        assertEquals(562.5f, rect.height, 0.01f);
        assertEquals(0f, rect.x);
        assertEquals((1000f - 562.5f) * 0.5f, rect.y, 0.01f);
    }

    @Test
    void stretchFillsSurface() {
        Rect4 rect = new Rect4();
        ViewportLayout.compute(800, 600, 16f / 9f, ViewportFitMode.STRETCH, rect);
        assertEquals(0f, rect.x);
        assertEquals(0f, rect.y);
        assertEquals(800f, rect.width);
        assertEquals(600f, rect.height);
    }

    @Test
    void cropFillsSurface() {
        Rect4 rect = new Rect4();
        ViewportLayout.compute(1000, 1000, 16f / 9f, ViewportFitMode.CROP, rect);
        assertEquals(0f, rect.x);
        assertEquals(0f, rect.y);
        assertEquals(1000f, rect.width);
        assertEquals(1000f, rect.height);
    }

    @Test
    void fixedFillsSurface() {
        Rect4 rect = new Rect4();
        ViewportLayout.compute(640, 480, 2f, ViewportFitMode.FIXED, rect);
        assertEquals(640f, rect.width);
        assertEquals(480f, rect.height);
    }
}
