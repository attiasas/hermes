package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.EntityId;

/**
 * Context passed to component deserializers for the entity currently being built.
 */
public interface ComponentContext {

    EntityId entityId();

    EntityKind kind();

    String entityName();

    <T extends Component> T sibling(Class<T> type);
}
