package dev.hermes.core.lighting;

import com.badlogic.gdx.graphics.g3d.Environment;
import dev.hermes.api.ecs.EntityStore;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/** Per-{@link EntityStore} cache of compiled libGDX lighting environments. */
public final class LightingRuntime {

    private static final Map<EntityStore, Environment> ENTRIES = new WeakHashMap<>();

    private LightingRuntime() {}

    public static Environment require(EntityStore entities) {
        Environment environment = ENTRIES.get(entities);
        if (environment == null) {
            throw new IllegalStateException("No compiled lighting environment for this EntityStore");
        }
        return environment;
    }

    public static void publish(EntityStore entities, Environment next) {
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(next, "next");
        ENTRIES.put(entities, next);
    }

    public static void remove(EntityStore entities) {
        if (entities == null) {
            return;
        }
        ENTRIES.remove(entities);
    }
}
