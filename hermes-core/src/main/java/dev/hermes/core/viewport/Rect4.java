package dev.hermes.core.viewport;

/**
 * Pixel rectangle (x, y, width, height). Internal to core viewport until API types land in Task 3.
 */
public final class Rect4 {

    public float x;
    public float y;
    public float width;
    public float height;

    public Rect4 set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }
}
