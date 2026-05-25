package dev.hermes.api.viewport;

import dev.hermes.api.math.Rect4;

/**
 * Public snapshot of a render target: pixel dimensions and letterbox viewport rect.
 */
public final class RenderSurfaceDesc {

    private final String targetId;
    private final int pixelWidth;
    private final int pixelHeight;
    private final Rect4 viewportRect;

    public RenderSurfaceDesc(String targetId, int pixelWidth, int pixelHeight, Rect4 viewportRect) {
        this.targetId = targetId;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.viewportRect = viewportRect;
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

    public void viewportRect(Rect4 out) {
        out.set(viewportRect.x, viewportRect.y, viewportRect.width, viewportRect.height);
    }
}
