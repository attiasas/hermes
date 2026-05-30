package dev.hermes.core.ecs;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.EntityTypeRegistry;
import dev.hermes.api.ecs.WorldManager;

public final class WorldManagerImpl implements WorldManager {

    private final EntityStoreImpl entities;

    public WorldManagerImpl() {
        this(new EntityTypeRegistryImpl(), new ComponentRegistryImpl());
    }

    public WorldManagerImpl(EntityTypeRegistry types, ComponentRegistryImpl registry) {
        EntityFactory factory = new EntityFactory(types, registry);
        this.entities = new EntityStoreImpl(factory);
    }

    @Override
    public EntityStore entities() {
        return entities;
    }
}
