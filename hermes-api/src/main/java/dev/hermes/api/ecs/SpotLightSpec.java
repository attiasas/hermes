package dev.hermes.api.ecs;

/** Static spot light entry parsed from a scene {@code lighting.spot} array. */
public final class SpotLightSpec {
    private final float[] position;
    private final float[] color;
    private final float intensity;
    private final float range;
    private final float[] direction;
    private final float cutoffAngle;
    private final float exponent;

    public SpotLightSpec(
            float[] position,
            float[] color,
            float intensity,
            float range,
            float[] direction,
            float cutoffAngle,
            float exponent) {
        this.position = position == null ? new float[0] : position.clone();
        this.color = color == null ? new float[0] : color.clone();
        this.intensity = intensity;
        this.range = range;
        this.direction = direction == null ? new float[0] : direction.clone();
        this.cutoffAngle = cutoffAngle;
        this.exponent = exponent;
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

    public float[] direction() {
        return direction;
    }

    public float cutoffAngle() {
        return cutoffAngle;
    }

    public float exponent() {
        return exponent;
    }
}
