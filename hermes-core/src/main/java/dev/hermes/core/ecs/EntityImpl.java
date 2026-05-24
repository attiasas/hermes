package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.EntityKind;

final class EntityImpl implements Entity {

    private final EntityId id;
    private final String name;
    private final EntityKind kind;

    EntityImpl(EntityId id, String name) {
        this(id, name, EntityKind.UNSET);
    }

    EntityImpl(EntityId id, String name, EntityKind kind) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.kind = kind == null ? EntityKind.UNSET : kind;
    }

    @Override
    public EntityId id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public EntityKind kind() {
        return kind;
    }
}
