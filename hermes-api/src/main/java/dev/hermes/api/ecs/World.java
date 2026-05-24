package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;

import java.util.Collection;

/**
 * ECS world holding entities and their components.
 */
public interface World {

    Entity createEntity(String name);

    Entity createEntity(String name, EntityKind kind);

    void clear();

    void removeEntity(EntityId id);

    Entity findByName(String name);

    Entity getEntity(EntityId id);

    <T extends Component> void addComponent(EntityId id, T component);

    <T extends Component> T getComponent(EntityId id, Class<T> type);

    <T extends Component> boolean hasComponent(EntityId id, Class<T> type);

    <T extends Component> void removeComponent(EntityId id, Class<T> type);

    Collection<Entity> entities();

    <T extends Component> Collection<Entity> entitiesWith(Class<T> componentType);

    Collection<Entity> entitiesWithKind(EntityKind kind);

    int entityCount();
}
