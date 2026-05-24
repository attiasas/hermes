package dev.hermes.api.scene;

/**
 * Populates a scene world (JSON assets, procedural content, etc.).
 */
@FunctionalInterface
public interface SceneSource {

    void populate(SceneLoadContext ctx);
}
