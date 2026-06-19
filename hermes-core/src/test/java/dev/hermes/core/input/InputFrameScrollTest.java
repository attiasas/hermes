package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class InputFrameScrollTest {

    @Test
    void inputFrame_carriesScrollDelta() {
        InputFrame frame = InputFrame.builder().scroll(0f, -2f).build();
        assertEquals(-2f, frame.scrollY(), 0.001f);
    }

    @Test
    void pointerScroll_factorySetsScrollY() {
        InputFrame frame = InputFrame.pointerScroll(320f, 240f, -1.5f);
        assertEquals(-1.5f, frame.scrollY(), 0.001f);
        assertEquals(320f, frame.pointerX(), 0.001f);
    }
}
