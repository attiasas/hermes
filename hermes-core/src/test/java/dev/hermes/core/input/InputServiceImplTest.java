package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputKey;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class InputServiceImplTest {

    private InputServiceImpl input;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        InputProfile profile = InputProfileLoader.load("input/profile.json");
        input = new InputServiceImpl(new HermesEngineImpl(), profile);
    }

    @Test
    void pollFrame_mapsSelectOnPointerClick() {
        input.pollFrame(InputFrame.pointerJustPressed(10, 20, InputButton.LEFT));
        assertTrue(input.actions().justPressed("select"));
        assertEquals(10f, input.devices().pointer().screenX(), 0.0001f);
        assertEquals(20f, input.devices().pointer().screenY(), 0.0001f);
    }

    @Test
    void pollFrame_mapsMoveXFromKeyboard() {
        input.pollFrame(InputFrame.withKeyboardPressed(InputKey.D));
        assertEquals(1f, input.actions().axis("move_x"), 0.0001f);
        assertTrue(input.devices().keyboard().pressed(InputKey.D));
    }
}
