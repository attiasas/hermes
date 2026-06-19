package dev.hermes.core.input;

import com.badlogic.gdx.Input.Keys;
import dev.hermes.api.input.InputKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class InputKeyLibGdxParityTest {

    @Test
    void constants_matchLibGdxKeys() {
        assertEquals(Keys.SPACE, InputKey.SPACE);
        assertEquals(Keys.ESCAPE, InputKey.ESCAPE);
        assertEquals(Keys.W, InputKey.W);
        assertEquals(Keys.UP, InputKey.UP);
        assertEquals(Keys.ENTER, InputKey.ENTER);
        assertEquals(Keys.Q, InputKey.Q);
        assertEquals(Keys.E, InputKey.E);
    }

    @Test
    void byName_matchesLibGdxKeys() {
        assertEquals(Keys.ESCAPE, InputKey.byName("escape"));
        assertEquals(Keys.SPACE, InputKey.byName("space"));
        assertEquals(Keys.ENTER, InputKey.byName("ENTER"));
        assertEquals(Keys.Q, InputKey.byName("Q"));
        assertEquals(Keys.E, InputKey.byName("E"));
    }
}
