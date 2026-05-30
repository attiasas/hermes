package dev.hermes.core.lighting;

import java.util.List;
import java.util.Optional;

/** Parsed scene-level {@code lighting} block from scene JSON. */
public final class SceneLightingBlock {

    private final Optional<AmbientEntry> ambient;
    private final Optional<DirectionalEntry> directional;
    private final List<PointLightEntry> pointLights;
    private final List<SpotLightEntry> spotLights;

    public SceneLightingBlock(
            Optional<AmbientEntry> ambient,
            Optional<DirectionalEntry> directional,
            List<PointLightEntry> pointLights,
            List<SpotLightEntry> spotLights) {
        this.ambient = ambient;
        this.directional = directional;
        this.pointLights = pointLights == null ? List.of() : List.copyOf(pointLights);
        this.spotLights = spotLights == null ? List.of() : List.copyOf(spotLights);
    }

    public Optional<AmbientEntry> ambient() {
        return ambient;
    }

    public Optional<DirectionalEntry> directional() {
        return directional;
    }

    public List<PointLightEntry> pointLights() {
        return pointLights;
    }

    public List<SpotLightEntry> spotLights() {
        return spotLights;
    }

    public static final class AmbientEntry {
        private final float[] color;
        private final float intensity;

        public AmbientEntry(float[] color, float intensity) {
            this.color = color;
            this.intensity = intensity;
        }

        public float[] color() {
            return color;
        }

        public float intensity() {
            return intensity;
        }
    }

    public static final class DirectionalEntry {
        private final float[] color;
        private final float intensity;
        private final float[] direction;

        public DirectionalEntry(float[] color, float intensity, float[] direction) {
            this.color = color;
            this.intensity = intensity;
            this.direction = direction;
        }

        public float[] color() {
            return color;
        }

        public float intensity() {
            return intensity;
        }

        public float[] direction() {
            return direction;
        }
    }

    public static final class PointLightEntry {
        private final float[] position;
        private final float[] color;
        private final float intensity;
        private final float range;

        public PointLightEntry(float[] position, float[] color, float intensity, float range) {
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

    public static final class SpotLightEntry {
        private final float[] position;
        private final float[] color;
        private final float intensity;
        private final float range;
        private final float[] direction;
        private final float cutoffAngle;
        private final float exponent;

        public SpotLightEntry(
                float[] position,
                float[] color,
                float intensity,
                float range,
                float[] direction,
                float cutoffAngle,
                float exponent) {
            this.position = position;
            this.color = color;
            this.intensity = intensity;
            this.range = range;
            this.direction = direction;
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
}
