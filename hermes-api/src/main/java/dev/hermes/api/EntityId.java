package dev.hermes.api;

import java.util.Objects;

/**
 * Stable identifier for an entity in a Hermes world.
 */
public final class EntityId {

  private final long value;

  public EntityId(long value) {
    this.value = value;
  }

  public long value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityId entityId = (EntityId) o;
    return value == entityId.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return "EntityId{" + "value=" + value + '}';
  }
}
