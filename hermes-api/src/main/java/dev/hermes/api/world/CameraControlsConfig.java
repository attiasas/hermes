package dev.hermes.api.world;

import dev.hermes.api.input.InputButton;

/** Built-in perspective camera pointer/keyboard controls (libGDX CameraInputController parity). */
public final class CameraControlsConfig {

    private CameraControlsMode mode = CameraControlsMode.ORBIT;
    private boolean enabled = true;
    private int rotateButton = InputButton.LEFT;
    private int translateButton = InputButton.RIGHT;
    private int forwardButton = InputButton.MIDDLE;
    private float rotateAngle = 360f;
    private float translateUnits = 10f;
    private float scrollFactor = -0.1f;
    private boolean scrollZoom = true;
    private boolean translateTarget = true;
    private boolean forwardTarget = true;
    private boolean scrollTarget = false;
    private float velocity = 5f;
    private float degreesPerPixel = 0.5f;

    public static CameraControlsConfig orbitDefaults() {
        return new CameraControlsConfig();
    }

    public static CameraControlsConfig firstPersonDefaults() {
        CameraControlsConfig config = new CameraControlsConfig();
        config.mode = CameraControlsMode.FIRST_PERSON;
        return config;
    }

    public static CameraControlsConfig disabled() {
        CameraControlsConfig config = new CameraControlsConfig();
        config.enabled = false;
        return config;
    }

    public CameraControlsMode mode() {
        return mode;
    }

    public void setMode(CameraControlsMode mode) {
        this.mode = mode == null ? CameraControlsMode.ORBIT : mode;
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

    public boolean translateTarget() {
        return translateTarget;
    }

    public void setTranslateTarget(boolean translateTarget) {
        this.translateTarget = translateTarget;
    }

    public boolean forwardTarget() {
        return forwardTarget;
    }

    public void setForwardTarget(boolean forwardTarget) {
        this.forwardTarget = forwardTarget;
    }

    public boolean scrollTarget() {
        return scrollTarget;
    }

    public void setScrollTarget(boolean scrollTarget) {
        this.scrollTarget = scrollTarget;
    }

    public float velocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public float degreesPerPixel() {
        return degreesPerPixel;
    }

    public void setDegreesPerPixel(float degreesPerPixel) {
        this.degreesPerPixel = degreesPerPixel;
    }
}
