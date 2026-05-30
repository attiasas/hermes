package dev.hermes.core.render;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Placeholder pass for pipeline types not yet implemented (post, particles, compute).
 */
final class UnimplementedGraphPass implements RenderGraphPass {

    private static final Set<String> LOGGED = Collections.synchronizedSet(new HashSet<>());

    private final String id;
    private final PipelineDocument.PassType type;

    UnimplementedGraphPass(String id, PipelineDocument.PassType type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render(EntityStore entities, RenderSurface surface, BoundCamera bound) {
        String key = type.name() + ":" + id;
        if (LOGGED.add(key)) {
            System.err.println(
                    "Render pipeline pass \""
                            + id
                            + "\" (type "
                            + type.jsonName()
                            + ") is not implemented yet; skipping.");
        }
    }

    @Override
    public void dispose() {
    }
}
