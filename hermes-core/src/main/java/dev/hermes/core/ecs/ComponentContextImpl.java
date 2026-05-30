package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.ComponentContext;
import dev.hermes.api.ecs.EntityKind;

import java.util.Map;

final class ComponentContextImpl implements ComponentContext {

    private final EntityId entityId;
    private final EntityKind kind;
    private final String entityName;
    private final Map<Class<? extends Component>, Component> siblings;

    ComponentContextImpl(
            EntityId entityId,
            EntityKind kind,
            String entityName,
            Map<Class<? extends Component>, Component> siblings) {
        this.entityId = entityId;
        this.kind = kind;
        this.entityName = entityName;
        this.siblings = siblings;
    }

    @Override
    public EntityId entityId() {
        return entityId;
    }

    @Override
    public EntityKind kind() {
        return kind;
    }

    @Override
    public String entityName() {
        return entityName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T sibling(Class<T> type) {
        return (T) siblings.get(type);
    }
}
