package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ScrollWheelCaptureTest {

    private ScrollWheelCapture capture;

    @BeforeEach
    void setUp() {
        capture = new ScrollWheelCapture();
    }

    @Test
    void scrolled_accumulatesWheelDelta() {
        capture.scrolled(0f, -2f);
        capture.scrolled(0f, 1f);
        assertEquals(-1f, capture.takeScrollY(), 0.001f);
        assertEquals(0f, capture.takeScrollY(), 0.001f);
    }

    @Test
    void takeScroll_resetsAccumulator() {
        capture.scrolled(1.5f, 0f);
        assertEquals(1.5f, capture.takeScrollX(), 0.001f);
        assertEquals(0f, capture.takeScrollX(), 0.001f);
    }
}
