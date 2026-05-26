package dev.hermes.api.scene;

import dev.hermes.api.ecs.World;

import java.util.Optional;

/**
 * Handle to a loaded scene instance on the stack.
 */
public interface SceneHandle {

    String id();

    World world();

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
