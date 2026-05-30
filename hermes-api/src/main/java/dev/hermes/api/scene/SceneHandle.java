package dev.hermes.api.scene;

import dev.hermes.api.ecs.WorldManager;

import java.util.Optional;

/**
 * Handle to a loaded scene instance on the stack.
 */
public interface SceneHandle {

    String id();

    WorldManager manager();

    boolean paused();

    /**
     * Scene JSON {@code renderPipeline} override, when the loaded scene file declares one.
     */
    Optional<String> renderPipelineOverride();

    /**
     * Scene JSON {@code inputContext} override, when the loaded scene file declares one.
     */
    Optional<String> inputContext();
}
