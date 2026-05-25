package dev.hermes.api.math;

public final class Rect4 {

    public float x;
    public float y;
    public float width;
    public float height;

    public Rect4() {
    }

    public Rect4(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect4 set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }
}
