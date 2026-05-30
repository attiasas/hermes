package dev.hermes.core.ecs;

import dev.hermes.api.ecs.EntityStore;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Loads a scene document into an entity store using an entity factory.
 */
final class SceneParser {

    private SceneParser() {
    }

    static SceneLoadMetadata loadIntoEntities(
            String scenePath, String json, EntityStore entities, EntityFactory factory) {
        SceneDocument document = SceneDocument.parse(scenePath, json);
        for (SceneDocument.EntitySpec entitySpec : document.entities()) {
            Map<String, JsonValue> instanceComponents = new LinkedHashMap<>();
            for (SceneDocument.ComponentSpec componentSpec : entitySpec.components()) {
                instanceComponents.put(componentSpec.typeName(), componentSpec.properties());
            }
            factory.create(
                    scenePath,
                    entities,
                    entitySpec.id(),
                    entitySpec.kind(),
                    instanceComponents);
        }
        return new SceneLoadMetadata(
                document.renderPipeline(), document.inputContext(), document.uiConfig());
    }
}
