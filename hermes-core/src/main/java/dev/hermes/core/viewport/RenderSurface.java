package dev.hermes.core.viewport;

import dev.hermes.api.math.Rect4;

/**
 * Immutable per-pass draw context: target id, pixel dimensions, and letterbox viewport rect.
 */
public final class RenderSurface {

    private final String targetId;
    private final int pixelWidth;
    private final int pixelHeight;
    private final Rect4 viewportRect;
    private final float aspect;

    private RenderSurface(String targetId, int pixelWidth, int pixelHeight, Rect4 viewportRect) {
        this.targetId = targetId;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.viewportRect = viewportRect;
        this.aspect = viewportRect.height > 0f ? viewportRect.width / viewportRect.height : 1f;
    }

    public static RenderSurface screen(int windowW, int windowH, Rect4 rect) {
        return new RenderSurface("screen", windowW, windowH, copyRect(rect, windowW, windowH));
    }

    public static RenderSurface framebuffer(String id, int w, int h, Rect4 rect) {
        return new RenderSurface(id, w, h, copyRect(rect, w, h));
    }

    private static Rect4 copyRect(Rect4 rect, int w, int h) {
        if (rect == null || (rect.width == 0f && rect.height == 0f)) {
            return new Rect4().set(0f, 0f, w, h);
        }
        return new Rect4().set(rect.x, rect.y, rect.width, rect.height);
    }

    public String targetId() {
        return targetId;
    }

    public int pixelWidth() {
        return pixelWidth;
    }

    public int pixelHeight() {
        return pixelHeight;
    }

    public Rect4 viewportRect() {
        return viewportRect;
    }

    public float aspect() {
        return aspect;
    }
}
