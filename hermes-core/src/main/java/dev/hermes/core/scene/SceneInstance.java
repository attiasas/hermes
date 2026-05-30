package dev.hermes.core.scene;

import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneAudioConfig;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.core.ecs.WorldManagerImpl;

import java.util.Optional;

/**
 * A loaded scene on the stack: dedicated world manager, definition, and pause state.
 */
public final class SceneInstance implements SceneHandle {

    private final String id;
    private final WorldManagerImpl manager;
    private final SceneDefinition definition;
    private final Optional<String> renderPipelineOverride;
    private final Optional<String> inputContextOverride;
    private final Optional<SceneUiConfig> uiConfig;
    private final Optional<SceneAudioConfig> audioConfig;
    private boolean paused;

    SceneInstance(
            String id,
            WorldManagerImpl manager,
            SceneDefinition definition,
            Optional<String> renderPipelineOverride,
            Optional<String> inputContextOverride,
            Optional<SceneUiConfig> uiConfig,
            Optional<SceneAudioConfig> audioConfig,
            boolean paused) {
        this.id = id;
        this.manager = manager;
        this.definition = definition;
        this.renderPipelineOverride = renderPipelineOverride == null ? Optional.empty() : renderPipelineOverride;
        this.inputContextOverride = inputContextOverride == null ? Optional.empty() : inputContextOverride;
        this.uiConfig = uiConfig == null ? Optional.empty() : uiConfig;
        this.audioConfig = audioConfig == null ? Optional.empty() : audioConfig;
        this.paused = paused;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public WorldManager manager() {
        return manager;
    }

    public SceneDefinition definition() {
        return definition;
    }

    @Override
    public Optional<String> renderPipelineOverride() {
        return renderPipelineOverride;
    }

    @Override
    public Optional<String> inputContext() {
        return inputContextOverride;
    }

    public Optional<SceneUiConfig> uiConfig() {
        return uiConfig;
    }

    public Optional<SceneAudioConfig> audioConfig() {
        return audioConfig;
    }

    @Override
    public boolean paused() {
        return paused;
    }

    void setPaused(boolean paused) {
        this.paused = paused;
    }
}
