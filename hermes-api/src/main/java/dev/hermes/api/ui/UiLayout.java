package dev.hermes.api.ui;

/**
 * Per-node layout: anchor, offsets, size, padding, and draw order.
 */
public final class UiLayout {

    private UiAnchor anchor = UiAnchor.TOP_LEFT;
    private float offsetX;
    private float offsetY;
    private float width = Float.NaN;
    private float height = Float.NaN;
    private float paddingLeft;
    private float paddingTop;
    private float paddingRight;
    private float paddingBottom;
    private int zIndex;

    public UiAnchor anchor() {
        return anchor;
    }

    public void setAnchor(UiAnchor anchor) {
        this.anchor = anchor == null ? UiAnchor.TOP_LEFT : anchor;
    }

    public float offsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float offsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float width() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float height() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float paddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public float paddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    public float paddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public float paddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public int zIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public static UiLayout stretch() {
        UiLayout layout = new UiLayout();
        layout.setAnchor(UiAnchor.STRETCH);
        return layout;
    }

    public static UiLayout of(UiAnchor anchor, float width, float height) {
        UiLayout layout = new UiLayout();
        layout.setAnchor(anchor);
        layout.setWidth(width);
        layout.setHeight(height);
        return layout;
    }
}
