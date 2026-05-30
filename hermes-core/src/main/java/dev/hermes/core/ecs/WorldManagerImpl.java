package dev.hermes.core.ecs;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;

public final class WorldManagerImpl implements WorldManager {

    private final EntityStoreImpl entities = new EntityStoreImpl();

    @Override
    public EntityStore entities() {
        return entities;
    }
}
