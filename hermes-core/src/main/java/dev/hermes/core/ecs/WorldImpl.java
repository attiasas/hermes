package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
final class WorldImpl implements World {

  private long nextId = 1;
  private final Map<EntityId, EntityImpl> entities = new LinkedHashMap<>();
  private final Map<EntityId, Map<Class<? extends Component>, Component>> components = new HashMap<>();
  private final Map<String, EntityId> names = new HashMap<>();

  @Override
  public Entity createEntity(String name) {
    return createEntity(name, EntityKind.UNSET);
  }

  @Override
  public Entity createEntity(String name, EntityKind kind) {
    EntityId id = new EntityId(nextId++);
    EntityImpl entity = new EntityImpl(id, name, kind);
    entities.put(id, entity);
    components.put(id, new HashMap<>());
    if (name != null && !name.isBlank()) {
      if (names.containsKey(name)) {
        throw new IllegalArgumentException("Duplicate entity name: " + name);
      }
      names.put(name, id);
    }
    return entity;
  }

  @Override
  public void clear() {
    entities.clear();
    components.clear();
    names.clear();
  }

  @Override
  public void removeEntity(EntityId id) {
    EntityImpl removed = entities.remove(id);
    if (removed != null && !removed.name().isBlank()) {
      names.remove(removed.name());
    }
    components.remove(id);
  }

  @Override
  public Entity findByName(String name) {
    EntityId id = names.get(name);
    return id == null ? null : entities.get(id);
  }

  @Override
  public Entity getEntity(EntityId id) {
    return entities.get(id);
  }

  @Override
  public <T extends Component> void addComponent(EntityId id, T component) {
    Map<Class<? extends Component>, Component> entityComponents = components.get(id);
    if (entityComponents == null) {
      throw new IllegalArgumentException("Unknown entity: " + id);
    }
    entityComponents.put(component.getClass(), component);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Component> T getComponent(EntityId id, Class<T> type) {
    Map<Class<? extends Component>, Component> entityComponents = components.get(id);
    if (entityComponents == null) {
      return null;
    }
    return (T) entityComponents.get(type);
  }

  @Override
  public <T extends Component> boolean hasComponent(EntityId id, Class<T> type) {
    Map<Class<? extends Component>, Component> entityComponents = components.get(id);
    return entityComponents != null && entityComponents.containsKey(type);
  }

  @Override
  public <T extends Component> void removeComponent(EntityId id, Class<T> type) {
    Map<Class<? extends Component>, Component> entityComponents = components.get(id);
    if (entityComponents != null) {
      entityComponents.remove(type);
    }
  }

  @Override
  public Collection<Entity> entities() {
    return Collections.unmodifiableCollection(new ArrayList<>(entities.values()));
  }

  @Override
  public <T extends Component> Collection<Entity> entitiesWith(Class<T> componentType) {
    List<Entity> result = new ArrayList<>();
    for (Map.Entry<EntityId, Map<Class<? extends Component>, Component>> entry : components.entrySet()) {
      if (entry.getValue().containsKey(componentType)) {
        Entity entity = entities.get(entry.getKey());
        if (entity != null) {
          result.add(entity);
        }
      }
    }
    return result;
  }

  @Override
  public Collection<Entity> entitiesWithKind(EntityKind kind) {
    EntityKind query = kind == null ? EntityKind.UNSET : kind;
    List<Entity> result = new ArrayList<>();
    for (Entity entity : entities.values()) {
      if (entity.kind().equals(query)) {
        result.add(entity);
      }
    }
    return result;
  }

  @Override
  public int entityCount() {
    return entities.size();
  }
}
