package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.List;

/** Parses scene JSON into entity specifications. */
final class SceneDocument {

  private final List<EntitySpec> entities;

  private SceneDocument(List<EntitySpec> entities) {
    this.entities = entities;
  }

  static SceneDocument parse(String scenePath, String json) {
    try {
      JsonValue root = new JsonReader().parse(json);
      JsonValue entitiesArray = root.get("entities");
      List<EntitySpec> entities = new ArrayList<>();
      if (entitiesArray != null && entitiesArray.isArray()) {
        for (JsonValue entityValue : entitiesArray) {
          if (!entityValue.isObject()) {
            continue;
          }
          String id = entityValue.has("id") ? entityValue.getString("id", "") : "";
          JsonValue componentsValue = entityValue.get("components");
          List<ComponentSpec> components = new ArrayList<>();
          if (componentsValue != null && componentsValue.isObject()) {
            for (JsonValue componentEntry : componentsValue) {
              String typeName = componentEntry.name;
              JsonValue properties = componentEntry.isObject() ? componentEntry : new JsonValue(JsonValue.ValueType.object);
              components.add(new ComponentSpec(typeName, properties));
            }
          }
          entities.add(new EntitySpec(id, components));
        }
      }
      return new SceneDocument(entities);
    } catch (Exception e) {
      throw new SceneLoadException("Scene '" + scenePath + "': invalid JSON: " + e.getMessage(), e);
    }
  }

  List<EntitySpec> entities() {
    return entities;
  }

  static final class EntitySpec {
    private final String id;
    private final List<ComponentSpec> components;

    EntitySpec(String id, List<ComponentSpec> components) {
      this.id = id;
      this.components = components;
    }

    String id() {
      return id;
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
