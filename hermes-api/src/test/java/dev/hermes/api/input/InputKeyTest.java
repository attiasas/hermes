package dev.hermes.api.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputKeyTest {

    @Test
    void byName_escape_isCaseInsensitive() {
        assertEquals(InputKey.ESCAPE, InputKey.byName("escape"));
        assertEquals(InputKey.ESCAPE, InputKey.byName("ESCAPE"));
        assertEquals(InputKey.SPACE, InputKey.byName("space"));
    }

    @Test
    void byName_unknownKey_throws() {
        assertThrows(IllegalArgumentException.class, () -> InputKey.byName("not-a-key"));
    }
}
