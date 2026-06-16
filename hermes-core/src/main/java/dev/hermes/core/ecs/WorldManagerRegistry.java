package dev.hermes.core.ecs;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;

import java.util.Map;
import java.util.WeakHashMap;

/** Associates entity stores with their owning {@link WorldManager} for camera resolution. */
final class WorldManagerRegistry {

    private static final Map<EntityStore, WorldManager> BY_STORE = new WeakHashMap<>();

    private WorldManagerRegistry() {}

    static void register(WorldManager manager) {
        if (manager == null) {
            return;
        }
        BY_STORE.put(manager.entities(), manager);
    }

    static WorldManager lookup(EntityStore entities) {
        return entities == null ? null : BY_STORE.get(entities);
    }
}
