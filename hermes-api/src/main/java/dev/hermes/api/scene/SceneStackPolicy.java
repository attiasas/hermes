package dev.hermes.api.scene;

/**
 * Controls whether stacked (non-active) scenes receive update and render ticks.
 */
public final class SceneStackPolicy {

    private final boolean updateStackedScenes;
    private final boolean renderStackedScenes;

    public SceneStackPolicy(boolean updateStackedScenes, boolean renderStackedScenes) {
        this.updateStackedScenes = updateStackedScenes;
        this.renderStackedScenes = renderStackedScenes;
    }

    public boolean updateStackedScenes() {
        return updateStackedScenes;
    }

    public boolean renderStackedScenes() {
        return renderStackedScenes;
    }

    public static SceneStackPolicy defaults() {
        return new SceneStackPolicy(false, false);
    }
}
