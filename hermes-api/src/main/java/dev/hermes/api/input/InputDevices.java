package dev.hermes.api.input;

/**
 * Direct access to immutable per-frame device snapshots.
 */
public interface InputDevices {

    KeyboardSnapshot keyboard();

    PointerSnapshot pointer();

    int gamepadCount();

    GamepadSnapshot gamepad(int index);
}
