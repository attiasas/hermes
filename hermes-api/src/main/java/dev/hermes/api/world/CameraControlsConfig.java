package dev.hermes.api.world;

import dev.hermes.api.input.InputButton;

/** Built-in perspective camera pointer controls (libGDX CameraInputController parity). */
public final class CameraControlsConfig {

    private boolean enabled = true;
    private int rotateButton = InputButton.LEFT;
    private int translateButton = InputButton.RIGHT;
    private int forwardButton = InputButton.MIDDLE;
    private float rotateAngle = 360f;
    private float translateUnits = 10f;
    private float scrollFactor = -0.1f;
    private boolean scrollZoom = true;

    public static CameraControlsConfig defaults() {
        return new CameraControlsConfig();
    }

    public static CameraControlsConfig disabled() {
        CameraControlsConfig config = new CameraControlsConfig();
        config.enabled = false;
        return config;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int rotateButton() {
        return rotateButton;
    }

    public void setRotateButton(int rotateButton) {
        this.rotateButton = rotateButton;
    }

    public int translateButton() {
        return translateButton;
    }

    public void setTranslateButton(int translateButton) {
        this.translateButton = translateButton;
    }

    public int forwardButton() {
        return forwardButton;
    }

    public void setForwardButton(int forwardButton) {
        this.forwardButton = forwardButton;
    }

    public float rotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(float rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

    public float translateUnits() {
        return translateUnits;
    }

    public void setTranslateUnits(float translateUnits) {
        this.translateUnits = translateUnits;
    }

    public float scrollFactor() {
        return scrollFactor;
    }

    public void setScrollFactor(float scrollFactor) {
        this.scrollFactor = scrollFactor;
    }

    public boolean scrollZoom() {
        return scrollZoom;
    }

    public void setScrollZoom(boolean scrollZoom) {
        this.scrollZoom = scrollZoom;
    }
}
