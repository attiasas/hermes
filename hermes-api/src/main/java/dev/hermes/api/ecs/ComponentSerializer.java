package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** Serializes live components to inspector data and applies field edits. */
public interface ComponentSerializer {

  void serialize(Component component, MutableComponentData out);

  void applyField(Component component, String fieldName, Object value);
}
