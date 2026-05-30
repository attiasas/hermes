package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** Spot light; position from {@link Transform}, aim along local −Z unless overridden. */
public final class SpotLight implements Component {
    private boolean enabled = true;
    private float intensity = 1f;
    private final float[] color = {1f, 1f, 1f, 1f};
    private float range = 10f;
    private float cutoffAngle = 45f;
    private float exponent = 1f;
    private final float[] direction = {0f, 0f, -1f};
    private boolean directionOverridden;

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float intensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public float[] color() {
        return color;
    }

    public void setColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    public float range() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public float cutoffAngle() {
        return cutoffAngle;
    }

    public void setCutoffAngle(float cutoffAngle) {
        this.cutoffAngle = cutoffAngle;
    }

    public float exponent() {
        return exponent;
    }

    public void setExponent(float exponent) {
        this.exponent = exponent;
    }

    public float[] direction() {
        return direction;
    }

    public void setDirection(float x, float y, float z) {
        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
        directionOverridden = true;
    }

    public boolean directionOverridden() {
        return directionOverridden;
    }
}
