package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;

import java.util.Collection;

/**
 * ECS entity store holding entities and their components.
 */
public interface EntityStore {

    Entity create(String name);

    Entity create(String name, EntityKind kind);

    Entity spawn(String kind);

    Entity spawn(String kind, String name);

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

    void clear();
}
