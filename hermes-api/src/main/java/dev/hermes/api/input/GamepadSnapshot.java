package dev.hermes.api.input;

/**
 * Immutable gamepad state for the current frame (button/axis codes match libGDX gamepad constants).
 */
public interface GamepadSnapshot {

    boolean pressed(int button);

    boolean justPressed(int button);

    boolean justReleased(int button);

    /** Axis value in {@code [-1, 1]} (deadzone applied at poll time). */
    float axis(int axis);
}
