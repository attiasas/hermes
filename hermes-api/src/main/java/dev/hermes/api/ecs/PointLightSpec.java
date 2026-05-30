package dev.hermes.api.ecs;

public final class PointLightSpec {
    private final float[] position;
    private final float[] color;
    private final float intensity;
    private final float range;

    public PointLightSpec(float[] position, float[] color, float intensity, float range) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
        this.range = range;
    }

    public float[] position() {
        return position;
    }

    public float[] color() {
        return color;
    }

    public float intensity() {
        return intensity;
    }

    public float range() {
        return range;
    }
}
