package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;

/** Two-phase loader: decode off-thread (Phase A), upload on render thread (Phase B). */
public interface ResourceLoader {

    ResourceKind kind();

    /** Phase A — read and decode asset bytes; thread-safe. */
    DecodedPayload decode(String path);

    /** Phase B — create GPU resource from decoded payload; render thread only. */
    Object upload(DecodedPayload decoded);

    void dispose(Object resource);
}
