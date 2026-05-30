package dev.hermes.api.ecs;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Parsed entity type template from {@code entities/<kind>/type.json}.
 */
public interface EntityTypeDefinition {

    String kind();

    JsonValue componentsJson();
}
