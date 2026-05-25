package dev.hermes.api;

import dev.hermes.api.ecs.EntityKind;

/**
 * Handle for a logical game object composed of {@link Component}s (ECS; expanded in Phase 2).
 */
public interface Entity {

    EntityId id();

    /**
     * Optional logical name from scene JSON; empty when unnamed.
     */
    String name();

    /**
     * Logical entity type; {@link EntityKind#UNSET} when unspecified.
     */
    EntityKind kind();
}
