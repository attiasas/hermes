package dev.hermes.api.ui;

/**
 * Immutable loaded UI document: design resolution and root widget tree.
 */
public final class UiDocument {

    private final UiNode root;
    private final float designWidth;
    private final float designHeight;

    public UiDocument(UiNode root, float designWidth, float designHeight) {
        if (root == null) {
            throw new IllegalArgumentException("Root node is required");
        }
        this.root = root;
        this.designWidth = designWidth;
        this.designHeight = designHeight;
    }

    public UiNode root() {
        return root;
    }

    public float designWidth() {
        return designWidth;
    }

    public float designHeight() {
        return designHeight;
    }
}
