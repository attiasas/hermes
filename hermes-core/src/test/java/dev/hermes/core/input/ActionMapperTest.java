package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputKey;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ActionMapperTest {

    private InputProfile profile;
    private ActionMapper mapper;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        profile = InputProfileLoader.load("input/profile.json");
        mapper = new ActionMapper(profile);
    }

    @Test
    void justPressed_select_whenPointerLeftClicked() {
        InputActionsState state = new InputActionsState();
        InputFrame frame = InputFrame.pointerJustPressed(100, 200, InputButton.LEFT);
        mapper.apply(frame, "gameplay", state);
        assertTrue(state.justPressed("select"));
    }

    @Test
    void axis_moveX_fromKeyboardScale() {
        InputActionsState state = new InputActionsState();
        InputFrame frame = InputFrame.withKeyboardPressed(InputKey.D);
        mapper.apply(frame, "gameplay", state);
        assertEquals(1f, state.axis("move_x"), 0.0001f);

        state = new InputActionsState();
        frame = InputFrame.withKeyboardPressed(InputKey.A);
        mapper.apply(frame, "gameplay", state);
        assertEquals(-1f, state.axis("move_x"), 0.0001f);
    }

    @Test
    void axis_moveX_clampsCombinedKeyboardAndGamepad() {
        InputActionsState state = new InputActionsState();
        InputFrame frame =
                InputFrame.builder()
                        .keyboardPressed(InputKey.D)
                        .gamepadAxis(0, 1f)
                        .build();
        mapper.apply(frame, "gameplay", state);
        assertEquals(1f, state.axis("move_x"), 0.0001f);
    }

    @Test
    void gamepadAxis_appliesDeadzone() {
        InputActionsState state = new InputActionsState();
        InputFrame frame = InputFrame.withGamepadAxis(0, 0.1f);
        mapper.apply(frame, "gameplay", state);
        assertEquals(0f, state.axis("move_x"), 0.0001f);

        state = new InputActionsState();
        frame = InputFrame.withGamepadAxis(0, 0.5f);
        mapper.apply(frame, "gameplay", state);
        assertEquals(0.5f, state.axis("move_x"), 0.0001f);
    }

    @Test
    void binding_skippedWhenContextDoesNotMatch() {
        String json =
                "{"
                        + "\"version\":1,"
                        + "\"context\":\"gameplay\","
                        + "\"actions\":{\"select\":{\"type\":\"button\"}},"
                        + "\"bindings\":[{\"action\":\"select\",\"source\":\"pointer\","
                        + "\"button\":\"LEFT\",\"when\":\"justPressed\",\"context\":\"gameplay\"}]"
                        + "}";
        ActionMapper scoped = new ActionMapper(InputProfileLoader.parse(json));
        InputActionsState state = new InputActionsState();
        InputFrame frame = InputFrame.pointerJustPressed(0, 0, InputButton.LEFT);
        scoped.apply(frame, "menu", state);
        assertFalse(state.justPressed("select"));
        scoped.apply(frame, "gameplay", state);
        assertTrue(state.justPressed("select"));
    }
}
