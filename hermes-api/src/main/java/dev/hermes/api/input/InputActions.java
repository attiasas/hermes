package dev.hermes.api.input;

/**
 * Remapped input actions from the active profile context (immutable for the current frame).
 */
public interface InputActions {

    boolean pressed(String action);

    boolean justPressed(String action);

    boolean justReleased(String action);

    /** Axis value in {@code [-1, 1]}. */
    float axis(String action);

    /** Writes 2D axis to {@code out[0]=x}, {@code out[1]=y}. */
    void axis2(String action, float[] out);

    String context();
}
