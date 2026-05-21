package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.ecs.ComponentData;
import dev.hermes.api.ecs.ComponentDeserializer;
import dev.hermes.api.ecs.ComponentInspectorDescriptor;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.ComponentSerializer;
import dev.hermes.api.ecs.MutableComponentData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ComponentRegistryImpl implements ComponentRegistry {

  private final Map<String, Registration> registrations = new HashMap<>();
  private final Map<Class<? extends Component>, Registration> byType = new HashMap<>();

  ComponentRegistryImpl() {}

  @Override
  public void register(
      String typeName,
      Class<? extends Component> type,
      ComponentDeserializer deserializer,
      ComponentSerializer serializer,
      ComponentInspectorDescriptor descriptor) {
    if (typeName == null || typeName.isBlank()) {
      throw new IllegalArgumentException("typeName is required");
    }
    Registration registration = new Registration(type, deserializer, serializer, descriptor);
    registrations.put(typeName, registration);
    byType.put(type, registration);
  }

  @Override
  public boolean isRegistered(String typeName) {
    return registrations.containsKey(typeName);
  }

  Component deserialize(String scenePath, String entityName, String typeName, ComponentData data) {
    Registration registration = registrations.get(typeName);
    if (registration == null) {
      throw new SceneLoadException(
          formatUnknownComponent(scenePath, entityName, typeName));
    }
    return registration.deserializer().deserialize(data);
  }

  public MutableComponentData serializeComponent(Component component) {
    Registration registration = byType.get(component.getClass());
    if (registration == null || registration.serializer() == null) {
      throw new IllegalArgumentException(
          "No serializer registered for component type: " + component.getClass().getName());
    }
    MutableComponentData data = new MutableComponentData();
    registration.serializer().serialize(component, data);
    return data;
  }

  public void applyField(Component component, String fieldName, Object value) {
    Registration registration = byType.get(component.getClass());
    if (registration == null || registration.serializer() == null) {
      throw new IllegalArgumentException(
          "No serializer registered for component type: " + component.getClass().getName());
    }
    registration.serializer().applyField(component, fieldName, value);
  }

  public ComponentInspectorDescriptor descriptorFor(String typeName) {
    Registration registration = registrations.get(typeName);
    return registration == null ? null : registration.descriptor();
  }

  public List<Map.Entry<String, Class<? extends Component>>> registeredTypes() {
    List<Map.Entry<String, Class<? extends Component>>> result = new ArrayList<>();
    for (Map.Entry<String, Registration> entry : registrations.entrySet()) {
      result.add(Map.entry(entry.getKey(), entry.getValue().componentType()));
    }
    return result;
  }

  public Class<? extends Component> componentTypeFor(String typeName) {
    Registration registration = registrations.get(typeName);
    if (registration == null) {
      throw new IllegalArgumentException("Unknown component type: " + typeName);
    }
    return registration.componentType();
  }

  private static String formatUnknownComponent(String scenePath, String entityName, String typeName) {
    String entityLabel = entityName == null || entityName.isBlank() ? "<unnamed>" : "'" + entityName + "'";
    return "Scene '"
        + scenePath
        + "': entity "
        + entityLabel
        + ": unknown component '"
        + typeName
        + "' (register via ComponentRegistry or ServiceLoader)";
  }

  private static final class Registration {
    private final Class<? extends Component> type;
    private final ComponentDeserializer deserializer;
    private final ComponentSerializer serializer;
    private final ComponentInspectorDescriptor descriptor;

    private Registration(
        Class<? extends Component> type,
        ComponentDeserializer deserializer,
        ComponentSerializer serializer,
        ComponentInspectorDescriptor descriptor) {
      this.type = type;
      this.deserializer = deserializer;
      this.serializer = serializer;
      this.descriptor = descriptor;
    }

    private ComponentDeserializer deserializer() {
      return deserializer;
    }

    private ComponentSerializer serializer() {
      return serializer;
    }

    private ComponentInspectorDescriptor descriptor() {
      return descriptor;
    }

    private Class<? extends Component> componentType() {
      return type;
    }
  }
}
