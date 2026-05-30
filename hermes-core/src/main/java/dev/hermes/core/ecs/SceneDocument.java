package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import dev.hermes.api.scene.SceneUiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses scene JSON into entity specifications and optional scene metadata.
 */
final class SceneDocument {

    private final List<EntitySpec> entities;
    private final String renderPipeline;
    private final String inputContext;
    private final SceneUiConfig uiConfig;

    private SceneDocument(
            List<EntitySpec> entities, String renderPipeline, String inputContext, SceneUiConfig uiConfig) {
        this.entities = entities;
        this.renderPipeline = renderPipeline;
        this.inputContext = inputContext;
        this.uiConfig = uiConfig;
    }

    static SceneDocument parse(String scenePath, String json) {
        try {
            JsonValue root = new JsonReader().parse(json);
            JsonValue entitiesArray = root.get("entities");
            List<EntitySpec> entities = new ArrayList<>();
            if (entitiesArray != null && entitiesArray.isArray()) {
                for (int i = 0; i < entitiesArray.size; i++) {
                    JsonValue entityValue = entitiesArray.get(i);
                    if (!entityValue.isObject()) {
                        throw new SceneParseException(
                                "Scene '" + scenePath + "': entities[" + i + "] must be an object.");
                    }
                    String id = entityValue.has("id") ? entityValue.getString("id", "") : "";
                    String kind = "";
                    if (entityValue.has("type")) {
                        kind = entityValue.getString("type", "");
                    } else if (entityValue.has("kind")) {
                        kind = entityValue.getString("kind", "");
                    }
                    JsonValue componentsValue = entityValue.get("components");
                    List<ComponentSpec> components = new ArrayList<>();
                    if (componentsValue != null && componentsValue.isObject()) {
                        for (JsonValue componentEntry : componentsValue) {
                            String typeName = componentEntry.name;
                            JsonValue properties = componentEntry.isObject() ? componentEntry : new JsonValue(JsonValue.ValueType.object);
                            components.add(new ComponentSpec(typeName, properties));
                        }
                    }
                    entities.add(new EntitySpec(id, kind, components));
                }
            }
            String renderPipeline = null;
            if (root.has("renderPipeline")) {
                renderPipeline = root.getString("renderPipeline", "").trim();
                if (renderPipeline.isEmpty()) {
                    throw new SceneParseException(
                            "Scene '" + scenePath + "': \"renderPipeline\" must be non-empty when set.");
                }
            }
            String inputContext = null;
            if (root.has("inputContext")) {
                inputContext = root.getString("inputContext", "").trim();
                if (inputContext.isEmpty()) {
                    throw new SceneParseException(
                            "Scene '" + scenePath + "': \"inputContext\" must be non-empty when set.");
                }
            }
            SceneUiConfig uiConfig = null;
            if (root.has("ui")) {
                uiConfig = parseUiConfig(scenePath, root.get("ui"));
            }
            return new SceneDocument(entities, renderPipeline, inputContext, uiConfig);
        } catch (SceneParseException e) {
            throw e;
        } catch (Exception e) {
            throw new SceneLoadException("Scene '" + scenePath + "': invalid JSON: " + e.getMessage(), e);
        }
    }

    List<EntitySpec> entities() {
        return entities;
    }

    Optional<String> renderPipeline() {
        return Optional.ofNullable(renderPipeline);
    }

    Optional<String> inputContext() {
        return Optional.ofNullable(inputContext);
    }

    Optional<SceneUiConfig> uiConfig() {
        return Optional.ofNullable(uiConfig);
    }

    private static SceneUiConfig parseUiConfig(String scenePath, JsonValue uiValue) {
        if (uiValue == null) {
            throw new SceneParseException("Scene '" + scenePath + "': \"ui\" must be a string or object.");
        }
        if (uiValue.isString()) {
            String document = uiValue.asString().trim();
            if (document.isEmpty()) {
                throw new SceneParseException(
                        "Scene '" + scenePath + "': \"ui\" document path must be non-empty.");
            }
            return new SceneUiConfig(document);
        }
        if (uiValue.isObject()) {
            String document = uiValue.getString("document", "").trim();
            if (document.isEmpty()) {
                throw new SceneParseException(
                        "Scene '" + scenePath + "': \"ui.document\" must be non-empty when \"ui\" is an object.");
            }
            String fitMode = uiValue.has("fitMode") ? uiValue.getString("fitMode", "") : null;
            Float designAspect = uiValue.has("designAspect") ? uiValue.getFloat("designAspect", 0f) : null;
            return new SceneUiConfig(document, fitMode, designAspect);
        }
        throw new SceneParseException("Scene '" + scenePath + "': \"ui\" must be a string or object.");
    }

    static final class EntitySpec {
        private final String id;
        private final String kind;
        private final List<ComponentSpec> components;

        EntitySpec(String id, String kind, List<ComponentSpec> components) {
            this.id = id;
            this.kind = kind;
            this.components = components;
        }

        String id() {
            return id;
        }

        String kind() {
            return kind;
        }

        List<ComponentSpec> components() {
            return components;
        }
    }

    static final class ComponentSpec {
        private final String typeName;
        private final JsonValue properties;

        ComponentSpec(String typeName, JsonValue properties) {
            this.typeName = typeName;
            this.properties = properties;
        }

        String typeName() {
            return typeName;
        }

        JsonValue properties() {
            return properties;
        }
    }
}
