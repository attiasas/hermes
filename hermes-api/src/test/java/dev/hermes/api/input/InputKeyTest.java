package dev.hermes.api.input;

import com.badlogic.gdx.Input.Keys;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputKeyTest {

    @Test
    void space_matchesLibGdx() {
        assertEquals(Keys.SPACE, InputKey.SPACE);
    }

    @Test
    void byName_escape_isCaseInsensitive() {
        assertEquals(Keys.ESCAPE, InputKey.byName("escape"));
        assertEquals(Keys.ESCAPE, InputKey.byName("ESCAPE"));
        assertEquals(Keys.SPACE, InputKey.byName("space"));
    }
}
