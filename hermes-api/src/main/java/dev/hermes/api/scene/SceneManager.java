package dev.hermes.api.scene;

import dev.hermes.api.ecs.World;

import java.util.List;

/**
 * Multi-scene stack: registration, queued transitions, and active scene access.
 */
public interface SceneManager {

    void request(SceneChangeRequest request);

    void processPending();

    World activeWorld();

    SceneHandle active();

    List<SceneHandle> visibleScenes();

    /**
     * Scenes that receive system updates this frame (stack policy may differ from {@link #visibleScenes()}).
     */
    List<SceneHandle> updateScenes();

    SceneRegistry registry();

    int stackDepth();

    /**
     * Policy for stacked scene updates and rendering; defaults to active scene only.
     */
    void setStackPolicy(SceneStackPolicy policy);

    SceneStackPolicy stackPolicy();
}
