package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** Registers component types available for JSON scene loading. */
public interface ComponentRegistry {

  void register(
      String typeName,
      Class<? extends Component> type,
      ComponentDeserializer deserializer,
      ComponentSerializer serializer,
      ComponentInspectorDescriptor descriptor);

  default void register(
      String typeName, Class<? extends Component> type, ComponentDeserializer deserializer) {
    register(typeName, type, deserializer, null, null);
  }

  boolean isRegistered(String typeName);
}
