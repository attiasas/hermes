package dev.hermes.api.input;

/**
 * Immutable keyboard state for the current frame (key codes match libGDX {@code Keys}).
 */
public interface KeyboardSnapshot {

    boolean pressed(int key);

    boolean justPressed(int key);

    boolean justReleased(int key);
}
