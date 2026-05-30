package dev.hermes.core.ui;

import dev.hermes.api.math.Rect4;
import java.util.Collections;
import java.util.Map;

/** Layout pass output: SURFACE-space bounds keyed by widget node id. */
public final class UiLayoutResult {

    private final Map<String, Rect4> boundsById;

    UiLayoutResult(Map<String, Rect4> boundsById) {
        this.boundsById = Collections.unmodifiableMap(boundsById);
    }

    public Rect4 bounds(String nodeId) {
        Rect4 rect = boundsById.get(nodeId);
        if (rect == null) {
            throw new IllegalArgumentException("No layout bounds for node id: " + nodeId);
        }
        return rect;
    }

    public Map<String, Rect4> boundsById() {
        return boundsById;
    }
}
