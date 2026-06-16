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

    /** Horizontal scroll wheel delta this frame (0 if none). */
    default float scrollX() {
        return 0f;
    }

    /** Vertical scroll wheel delta this frame (0 if none). */
    default float scrollY() {
        return 0f;
    }
}
