package dev.hermes.api.ecs;

import dev.hermes.api.Component;

public final class AmbientLight implements Component {
    private boolean enabled = true;
    private float intensity = 1f;
    private final float[] color = {1f, 1f, 1f, 1f};

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
}
