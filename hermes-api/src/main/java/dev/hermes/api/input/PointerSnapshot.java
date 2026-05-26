package dev.hermes.api.input;

/**
 * Immutable pointer (mouse/touch) state for the current frame in window screen coordinates.
 */
public interface PointerSnapshot {

    float screenX();

    float screenY();

    boolean pressed();

    boolean justPressed(int button);

    boolean pressed(int button);
}
