package dev.hermes.core.ecs;

import dev.hermes.api.scene.SceneUiConfig;

import java.util.Optional;

/** Top-level metadata parsed from scene JSON (render pipeline, input context, etc.). */
public final class SceneLoadMetadata {

    private static final SceneLoadMetadata EMPTY =
            new SceneLoadMetadata(Optional.empty(), Optional.empty(), Optional.empty());

    private final Optional<String> renderPipeline;
    private final Optional<String> inputContext;
    private final Optional<SceneUiConfig> uiConfig;

    SceneLoadMetadata(
            Optional<String> renderPipeline,
            Optional<String> inputContext,
            Optional<SceneUiConfig> uiConfig) {
        this.renderPipeline = renderPipeline == null ? Optional.empty() : renderPipeline;
        this.inputContext = inputContext == null ? Optional.empty() : inputContext;
        this.uiConfig = uiConfig == null ? Optional.empty() : uiConfig;
    }

    public static SceneLoadMetadata empty() {
        return EMPTY;
    }

    public Optional<String> renderPipeline() {
        return renderPipeline;
    }

    public Optional<String> inputContext() {
        return inputContext;
    }

    public Optional<SceneUiConfig> uiConfig() {
        return uiConfig;
    }
}
