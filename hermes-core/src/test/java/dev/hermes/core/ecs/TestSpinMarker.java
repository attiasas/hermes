package dev.hermes.core.ecs;

import dev.hermes.api.Component;

/** Test-only component for verifying {@code $ref} resolution before deserialize. */
final class TestSpinMarker implements Component {

    private float centerX;
    private float centerY;
    private float speed;
    private float radius;

    float centerX() {
        return centerX;
    }

    void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    float centerY() {
        return centerY;
    }

    void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    float speed() {
        return speed;
    }

    void setSpeed(float speed) {
        this.speed = speed;
    }

    float radius() {
        return radius;
    }

    void setRadius(float radius) {
        this.radius = radius;
    }
}
