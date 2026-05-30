package dev.hermes.api.scene;

import java.util.Optional;

/**
 * Per-scene screen-space UI configuration from scene JSON {@code "ui"} field.
 */
public final class SceneUiConfig {

    private final String document;
    private final String fitMode;
    private final Float designAspect;

    public SceneUiConfig(String document) {
        this(document, "fit", null);
    }

    public SceneUiConfig(String document, String fitMode, Float designAspect) {
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("UI document path is required");
        }
        this.document = document;
        this.fitMode = fitMode == null || fitMode.isBlank() ? "fit" : fitMode;
        this.designAspect = designAspect;
    }

    public String document() {
        return document;
    }

    public String fitMode() {
        return fitMode;
    }

    public Optional<Float> designAspect() {
        return Optional.ofNullable(designAspect);
    }
}
