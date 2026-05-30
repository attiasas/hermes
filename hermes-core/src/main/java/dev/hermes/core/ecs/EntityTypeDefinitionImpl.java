package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.ecs.EntityTypeDefinition;

final class EntityTypeDefinitionImpl implements EntityTypeDefinition {

    private final String kind;
    private final JsonValue componentsJson;

    EntityTypeDefinitionImpl(String kind, JsonValue componentsJson) {
        this.kind = kind;
        this.componentsJson = componentsJson;
    }

    @Override
    public String kind() {
        return kind;
    }

    JsonValue componentsJson() {
        return componentsJson;
    }
}
