package dev.hermes.api.render;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.viewport.SceneViewport;

/**
 * Render pass context: target surface dimensions and viewport conversions for custom passes.
 */
public interface RenderContext {

    String renderTargetId();

    float surfaceWidth();

    float surfaceHeight();

    void viewportRect(Rect4 out);

    SceneViewport viewport(EntityStore entities);

    SceneViewport viewport(EntityStore entities, String cameraEntityName);
}
