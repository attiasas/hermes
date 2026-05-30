package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/**
 * Deserializes a component instance from scene JSON properties.
 */
@FunctionalInterface
public interface ComponentDeserializer {

    Component deserialize(ComponentData data, ComponentContext context);
}
