package dev.hermes.api.ecs;

/**
 * Grid layout for a sprite texture: column/row count and per-frame pixel size.
 */
public final class SpriteSheet {

    private int columns = 1;
    private int rows = 1;
    private int frameWidth = 1;
    private int frameHeight = 1;

    public int columns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int rows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int frameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int frameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }
}
