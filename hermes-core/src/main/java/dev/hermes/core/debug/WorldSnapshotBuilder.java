package dev.hermes.core.debug;

import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.FieldDescriptor;
import dev.hermes.api.ecs.MutableComponentData;
import dev.hermes.api.ecs.World;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.debug.ComponentSnapshot;
import dev.hermes.debug.EntitySnapshot;
import dev.hermes.debug.FieldKind;
import dev.hermes.debug.FieldSnapshot;
import dev.hermes.debug.WorldSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Builds HDP {@link WorldSnapshot} DTOs from a live ECS world. */
public final class WorldSnapshotBuilder {

  private final ComponentRegistryImpl registry;

  public WorldSnapshotBuilder(ComponentRegistryImpl registry) {
    this.registry = registry;
  }

  public WorldSnapshot build(World world, String scenePath, long frame) {
    List<EntitySnapshot> entities = new ArrayList<>();
    for (Entity entity : world.entities()) {
      entities.add(toEntitySnapshot(world, entity));
    }
    return new WorldSnapshot(frame, scenePath, entities);
  }

  private EntitySnapshot toEntitySnapshot(World world, Entity entity) {
    List<ComponentSnapshot> components = new ArrayList<>();
    for (Map.Entry<String, Class<? extends Component>> entry : registry.registeredTypes()) {
      Class<? extends Component> componentType = entry.getValue();
      if (!world.hasComponent(entity.id(), componentType)) {
        continue;
      }
      Component component = world.getComponent(entity.id(), componentType);
      components.add(toComponentSnapshot(entry.getKey(), component));
    }
    return new EntitySnapshot(Long.toString(entity.id().value()), entity.name(), components);
  }

  private ComponentSnapshot toComponentSnapshot(String typeName, Component component) {
    MutableComponentData data = registry.serializeComponent(component);
    var descriptor = registry.descriptorFor(typeName);
    List<FieldSnapshot> fields = new ArrayList<>();
    if (descriptor != null) {
      for (FieldDescriptor field : descriptor.fields()) {
        fields.add(
            new FieldSnapshot(
                field.name(),
                toDebugKind(field.kind()),
                field.editable(),
                readFieldValue(data, field)));
      }
    }
    return new ComponentSnapshot(typeName, data.copyProperties(), fields);
  }

  private static Object readFieldValue(MutableComponentData data, FieldDescriptor field) {
    return switch (field.kind()) {
      case FLOAT -> data.getFloat(field.name(), 0f);
      case INT -> data.getInt(field.name(), 0);
      case BOOLEAN -> data.getBoolean(field.name(), false);
      case STRING, ENUM -> data.getString(field.name(), "");
    };
  }

  private static FieldKind toDebugKind(dev.hermes.api.ecs.FieldKind kind) {
    return FieldKind.valueOf(kind.name());
  }
}
