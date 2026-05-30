package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * World-attached UI overlay: document tree anchored to another entity by name.
 */
public final class UiAttach implements Component {

    private String document;
    private String follow;
    private float offsetX;
    private float offsetY;
    private float offsetZ;
    private boolean visible = true;

    public String document() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    /**
     * Target entity name to follow; null when anchored to this entity's transform only.
     */
    public String follow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
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

    public float offsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
