package dev.hermes.api.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of custom render pass handlers keyed by pipeline {@code handler} id.
 */
public final class RenderPassRegistry {

    private final Map<String, RenderPass> passes = new HashMap<>();

    public void register(String handler, RenderPass pass) {
        if (handler == null || handler.isBlank()) {
            throw new IllegalArgumentException("handler id is required");
        }
        if (pass == null) {
            throw new IllegalArgumentException("pass is required");
        }
        passes.put(handler, pass);
    }

    public Optional<RenderPass> find(String handler) {
        if (handler == null || handler.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(passes.get(handler));
    }

    public RenderPass require(String handler) {
        return find(handler)
                .orElseThrow(() -> new IllegalArgumentException("no render pass registered for handler: " + handler));
    }

    public boolean isEmpty() {
        return passes.isEmpty();
    }
}
