package dev.hermes.core.resource;

import java.util.Objects;

final class ResourceCacheEntry {

    private final Object payload;
    private final Runnable onDispose;
    private int refs;

    ResourceCacheEntry(Object payload, Runnable onDispose) {
        this.payload = Objects.requireNonNull(payload, "payload");
        this.onDispose = Objects.requireNonNull(onDispose, "onDispose");
        this.refs = 1;
    }

    Object payload() {
        return payload;
    }

    int refs() {
        return refs;
    }

    void retain() {
        refs++;
    }

    boolean release() {
        if (refs <= 0) {
            return false;
        }
        refs--;
        return refs == 0;
    }

    void dispose() {
        onDispose.run();
    }
}
