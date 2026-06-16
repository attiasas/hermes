package dev.hermes.core.ecs;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.lighting.SceneLightingNames;
import dev.hermes.core.lighting.LightingDefaultsMapper;
import dev.hermes.core.world.WorldBlockApplier;

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
        return loadIntoManager(scenePath, json, null, entities, factory);
    }

    static SceneLoadMetadata loadIntoManager(
            String scenePath, String json, WorldManager manager, EntityStore entities, EntityFactory factory) {
        SceneDocument document = SceneDocument.parse(scenePath, json);
        for (SceneDocument.EntitySpec entitySpec : document.entities()) {
            if (SceneLightingNames.SCENE_ENTITY_NAME.equals(entitySpec.id())) {
                throw new SceneParseException(
                        "Scene '"
                                + scenePath
                                + "': entity id '"
                                + SceneLightingNames.SCENE_ENTITY_NAME
                                + "' is reserved.");
            }
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
        Entity sceneEntity = entities.create(SceneLightingNames.SCENE_ENTITY_NAME);
        SceneLightingState state = new SceneLightingState();
        document.lighting().ifPresent(spec -> LightingDefaultsMapper.apply(spec, state));
        entities.addComponent(sceneEntity.id(), state);
        if (manager != null) {
            document.world().ifPresent(block -> WorldBlockApplier.apply(manager.space(), block));
            document.camera().ifPresent(block -> {
                manager.camera().setSceneConfig(block.config());
                block.followEntity().ifPresent(manager.camera()::bindMain);
            });
        }
        return SceneLoadMetadata.fromDocument(document);
    }
}
