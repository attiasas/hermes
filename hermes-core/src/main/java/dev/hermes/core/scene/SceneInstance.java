package dev.hermes.core.scene;

import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.core.ecs.WorldImpl;

import java.util.Optional;

/**
 * A loaded scene on the stack: dedicated world, definition, and pause state.
 */
public final class SceneInstance implements SceneHandle {

    private final String id;
    private final WorldImpl world;
    private final SceneDefinition definition;
    private final Optional<String> renderPipelineOverride;
    private boolean paused;

    SceneInstance(
            String id,
            WorldImpl world,
            SceneDefinition definition,
            Optional<String> renderPipelineOverride,
            boolean paused) {
        this.id = id;
        this.world = world;
        this.definition = definition;
        this.renderPipelineOverride = renderPipelineOverride == null ? Optional.empty() : renderPipelineOverride;
        this.paused = paused;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public World world() {
        return world;
    }

    public SceneDefinition definition() {
        return definition;
    }

    @Override
    public Optional<String> renderPipelineOverride() {
        return renderPipelineOverride;
    }

    @Override
    public boolean paused() {
        return paused;
    }

    void setPaused(boolean paused) {
        this.paused = paused;
    }
}
