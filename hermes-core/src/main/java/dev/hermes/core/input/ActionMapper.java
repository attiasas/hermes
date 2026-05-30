package dev.hermes.core.input;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Maps device snapshots to remapped actions using an {@link InputProfile}. */
public final class ActionMapper {

    private final InputProfile profile;
    private final List<InputProfile.Binding> globalBindings;
    private final List<InputProfile.Binding> contextBindings;
    private final Set<String> buttonActions;

    public ActionMapper(InputProfile profile) {
        this.profile = profile;
        this.globalBindings = new ArrayList<>();
        this.contextBindings = new ArrayList<>();
        this.buttonActions = new HashSet<>();
        for (var entry : profile.actions().entrySet()) {
            if (entry.getValue() == InputProfile.ActionType.BUTTON) {
                buttonActions.add(entry.getKey());
            }
        }
        for (InputProfile.Binding binding : profile.bindings()) {
            if (binding.context() == null || "*".equals(binding.context())) {
                globalBindings.add(binding);
            } else {
                contextBindings.add(binding);
            }
        }
    }

    public void apply(InputFrame frame, String activeContext, InputActionsState state) {
        state.beginFrame(activeContext);
        applyBindings(frame, activeContext, globalBindings, state);
        applyBindings(frame, activeContext, contextBindings, state);
        clampAxes(state);
        state.finalizeEdges(buttonActions);
    }

    private void applyBindings(
            InputFrame frame,
            String activeContext,
            List<InputProfile.Binding> bindings,
            InputActionsState state) {
        for (InputProfile.Binding binding : bindings) {
            if (!contextApplies(binding.context(), activeContext)) {
                continue;
            }
            if (bindingActionType(binding.action()) == InputProfile.ActionType.AXIS) {
                applyAxisBinding(frame, binding, state);
            } else {
                applyButtonBinding(frame, binding, state);
            }
        }
    }

    private InputProfile.ActionType bindingActionType(String action) {
        InputProfile.ActionType type = profile.actions().get(action);
        if (type == null) {
            throw new InputProfileParseException("unknown action in binding: " + action);
        }
        return type;
    }

    private void applyAxisBinding(InputFrame frame, InputProfile.Binding binding, InputActionsState state) {
        float contribution = 0f;
        switch (binding.source()) {
            case KEYBOARD:
                if (frame.keyboardPressed(binding.key())) {
                    contribution = binding.scale();
                }
                break;
            case GAMEPAD:
                if (binding.axis() >= 0) {
                    float raw = frame.gamepadAxis(binding.axis());
                    contribution = binding.scale() * applyDeadzone(raw, profile.gamepadDeadzone());
                }
                break;
            case POINTER:
                break;
            default:
                break;
        }
        if (contribution != 0f) {
            state.addAxis(binding.action(), contribution);
        }
    }

    private void applyButtonBinding(InputFrame frame, InputProfile.Binding binding, InputActionsState state) {
        if (!deviceActive(frame, binding, binding.when())) {
            return;
        }
        switch (binding.when()) {
            case JUST_PRESSED:
                state.setJustPressed(binding.action());
                break;
            case JUST_RELEASED:
                state.setJustReleased(binding.action());
                break;
            case PRESSED:
            default:
                state.setPressed(binding.action(), true);
                break;
        }
    }

    private static boolean deviceActive(
            InputFrame frame, InputProfile.Binding binding, InputProfile.BindingWhen when) {
        switch (binding.source()) {
            case KEYBOARD:
                return keyboardActive(frame, binding.key(), when);
            case POINTER:
                return pointerActive(frame, binding.button(), when);
            case GAMEPAD:
                if (binding.gamepadButton() >= 0) {
                    return gamepadButtonActive(frame, binding.gamepadButton(), when);
                }
                return false;
            default:
                return false;
        }
    }

    private static boolean keyboardActive(InputFrame frame, int key, InputProfile.BindingWhen when) {
        switch (when) {
            case JUST_PRESSED:
                return frame.keyboardJustPressed(key);
            case JUST_RELEASED:
                return frame.keyboardJustReleased(key);
            case PRESSED:
            default:
                return frame.keyboardPressed(key);
        }
    }

    private static boolean pointerActive(InputFrame frame, int button, InputProfile.BindingWhen when) {
        switch (when) {
            case JUST_PRESSED:
                return frame.pointerJustPressed(button);
            case JUST_RELEASED:
                return frame.pointerJustReleased(button);
            case PRESSED:
            default:
                return frame.pointerPressed(button);
        }
    }

    private static boolean gamepadButtonActive(
            InputFrame frame, int button, InputProfile.BindingWhen when) {
        switch (when) {
            case JUST_PRESSED:
                return frame.gamepadJustPressed(button);
            case JUST_RELEASED:
                return frame.gamepadJustReleased(button);
            case PRESSED:
            default:
                return frame.gamepadPressed(button);
        }
    }

    private static boolean contextApplies(String bindingContext, String activeContext) {
        if (bindingContext == null || "*".equals(bindingContext)) {
            return true;
        }
        return bindingContext.equals(activeContext);
    }

    private void clampAxes(InputActionsState state) {
        for (var entry : profile.actions().entrySet()) {
            if (entry.getValue() != InputProfile.ActionType.AXIS) {
                continue;
            }
            state.setAxis(entry.getKey(), clamp(state.axis(entry.getKey())));
        }
    }

    private static float applyDeadzone(float value, float deadzone) {
        if (Math.abs(value) < deadzone) {
            return 0f;
        }
        return value;
    }

    private static float clamp(float value) {
        if (value < -1f) {
            return -1f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }
}
