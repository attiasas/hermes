package dev.hermes.core.ui;

/** Cached screen anchor for a {@link dev.hermes.api.ecs.UiAttach} entity. */
final class AttachAnchor {

    final float screenX;
    final float screenY;
    final String documentPath;

    AttachAnchor(float screenX, float screenY, String documentPath) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.documentPath = documentPath;
    }
}
