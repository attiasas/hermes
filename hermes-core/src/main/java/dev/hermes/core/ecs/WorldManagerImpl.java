package dev.hermes.core.ecs;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.EntityTypeRegistry;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.world.WorldSpace;
import dev.hermes.api.world.SceneCameraController;
import dev.hermes.core.world.WorldSpaceImpl;
import dev.hermes.core.world.SceneCameraControllerImpl;

public final class WorldManagerImpl implements WorldManager {

    private final EntityStoreImpl entities;
    private final WorldSpaceImpl space;
    private final SceneCameraControllerImpl camera;

    public WorldManagerImpl() {
        this(new EntityTypeRegistryImpl(), new ComponentRegistryImpl());
    }

    public WorldManagerImpl(EntityTypeRegistry types, ComponentRegistryImpl registry) {
        EntityFactory factory = new EntityFactory(types, registry);
        this.entities = new EntityStoreImpl(factory);
        this.space = new WorldSpaceImpl();
        this.camera = new SceneCameraControllerImpl(this);
        WorldManagerRegistry.register(this);
    }

    @Override
    public EntityStore entities() {
        return entities;
    }

    @Override
    public WorldSpace space() {
        return space;
    }

    @Override
    public SceneCameraController camera() {
        return camera;
    }
}
