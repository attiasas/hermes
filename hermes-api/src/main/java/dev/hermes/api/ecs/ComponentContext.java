package dev.hermes.api.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.EntityId;
import dev.hermes.api.resource.ResourceService;

/**
 * Context passed to component deserializers for the entity currently being built.
 */
public interface ComponentContext {

    EntityId entityId();

    EntityKind kind();

    String entityName();

    <T extends Component> T sibling(Class<T> type);

    /** Resource service for resolving {@code @alias} paths; null when unavailable. */
    default ResourceService resources() {
        return null;
    }
}
