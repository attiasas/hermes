package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;

final class EntityImpl implements Entity {

  private final EntityId id;
  private final String name;

  EntityImpl(EntityId id, String name) {
    this.id = id;
    this.name = name == null ? "" : name;
  }

  @Override
  public EntityId id() {
    return id;
  }

  @Override
  public String name() {
    return name;
  }
}
