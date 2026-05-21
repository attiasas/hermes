package dev.hermes.api.ecs;

import java.util.Objects;

/** Logical entity type identifier for scene-driven entity classification. */
public final class EntityKind {

  public static final EntityKind UNSET = new EntityKind("");

  private final String id;

  private EntityKind(String id) {
    this.id = id == null ? "" : id;
  }

  public static EntityKind of(String id) {
    return id == null || id.isEmpty() ? UNSET : new EntityKind(id);
  }

  public String id() {
    return id;
  }

  public boolean isUnset() {
    return id.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityKind that = (EntityKind) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return isUnset() ? "EntityKind{UNSET}" : "EntityKind{" + id + '}';
  }
}
