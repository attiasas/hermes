package dev.hermes.core.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;
import dev.hermes.api.input.InputButton;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** Polls libGDX keyboard, pointer, and gamepad state into an {@link InputFrame}. */
final class GdxInputReaders {

    private final int[] keyboardKeys;
    private final int[] pointerButtons;
    private final int[] gamepadButtons;
    private final int[] gamepadAxes;
    private final boolean pollGamepad;
    private final Set<Integer> previousKeyboardPressed = new HashSet<>();
    private final Set<Integer> previousPointerPressed = new HashSet<>();
    private final Set<Integer> previousGamepadPressed = new HashSet<>();

    private final ScrollWheelCapture scrollWheel = ScrollWheelCapture.shared();

    GdxInputReaders(InputProfile profile) {
        Set<Integer> keys = new LinkedHashSet<>();
        Set<Integer> buttons = new LinkedHashSet<>();
        Set<Integer> padButtons = new LinkedHashSet<>();
        Set<Integer> axes = new LinkedHashSet<>();
        for (InputProfile.Binding binding : profile.bindings()) {
            switch (binding.source()) {
                case KEYBOARD:
                    if (binding.key() >= 0) {
                        keys.add(binding.key());
                    }
                    break;
                case POINTER:
                    if (binding.button() >= 0) {
                        buttons.add(binding.button());
                    }
                    break;
                case GAMEPAD:
                    if (binding.gamepadButton() >= 0) {
                        padButtons.add(binding.gamepadButton());
                    }
                    if (binding.axis() >= 0) {
                        axes.add(binding.axis());
                    }
                    break;
                default:
                    break;
            }
        }
        buttons.add(InputButton.LEFT);
        buttons.add(InputButton.RIGHT);
        buttons.add(InputButton.MIDDLE);
        keyboardKeys = keys.stream().mapToInt(Integer::intValue).toArray();
        pointerButtons = buttons.stream().mapToInt(Integer::intValue).toArray();
        gamepadButtons = padButtons.stream().mapToInt(Integer::intValue).toArray();
        gamepadAxes = axes.stream().mapToInt(Integer::intValue).toArray();
        pollGamepad = gamepadButtons.length > 0 || gamepadAxes.length > 0;
    }

    InputFrame poll() {
        scrollWheel.installIfNeeded();
        InputFrame.Builder builder = InputFrame.builder();
        pollKeyboard(builder);
        pollPointer(builder);
        pollGamepad(builder);
        builder.scroll(scrollWheel.takeScrollX(), scrollWheel.takeScrollY());
        return builder.build();
    }

    private void pollKeyboard(InputFrame.Builder builder) {
        Set<Integer> pressed = new HashSet<>();
        for (int key : keyboardKeys) {
            boolean isPressed = Gdx.input.isKeyPressed(key);
            if (isPressed) {
                pressed.add(key);
                builder.keyboardPressed(key);
            }
            if (Gdx.input.isKeyJustPressed(key)) {
                builder.keyboardJustPressed(key);
            }
            if (previousKeyboardPressed.contains(key) && !isPressed) {
                builder.keyboardJustReleased(key);
            }
        }
        previousKeyboardPressed.clear();
        previousKeyboardPressed.addAll(pressed);
    }

    private void pollPointer(InputFrame.Builder builder) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        builder.pointer(x, y);
        Set<Integer> pressed = new HashSet<>();
        for (int button : pointerButtons) {
            boolean isPressed = Gdx.input.isButtonPressed(button);
            if (isPressed) {
                pressed.add(button);
                builder.pointerPressed(button);
            }
            if (Gdx.input.isButtonJustPressed(button)) {
                builder.pointerJustPressed(button);
            }
            if (previousPointerPressed.contains(button) && !isPressed) {
                builder.pointerJustReleased(button);
            }
        }
        previousPointerPressed.clear();
        previousPointerPressed.addAll(pressed);
    }

    private void pollGamepad(InputFrame.Builder builder) {
        if (!pollGamepad) {
            previousGamepadPressed.clear();
            return;
        }
        Array<Controller> controllers;
        try {
            controllers = Controllers.getControllers();
        } catch (RuntimeException ex) {
            previousGamepadPressed.clear();
            return;
        }
        if (controllers.size == 0) {
            previousGamepadPressed.clear();
            builder.connectedGamepadCount(0);
            return;
        }
        builder.connectedGamepadCount(1);
        Controller controller = controllers.first();
        for (int axis : gamepadAxes) {
            builder.gamepadAxis(axis, controller.getAxis(axis));
        }
        Set<Integer> pressed = new HashSet<>();
        for (int button : gamepadButtons) {
            boolean isPressed = controller.getButton(button);
            if (isPressed) {
                pressed.add(button);
                builder.gamepadPressed(button);
            }
            if (!previousGamepadPressed.contains(button) && isPressed) {
                builder.gamepadJustPressed(button);
            }
            if (previousGamepadPressed.contains(button) && !isPressed) {
                builder.gamepadJustReleased(button);
            }
        }
        previousGamepadPressed.clear();
        previousGamepadPressed.addAll(pressed);
    }
}
