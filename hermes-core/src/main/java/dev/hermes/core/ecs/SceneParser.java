package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.World;
import java.util.List;
import java.util.Optional;

/** Loads a scene document into a world using a component registry. */
final class SceneParser {

  private SceneParser() {}

  static Optional<String> loadIntoWorld(
      String scenePath, String json, World world, ComponentRegistryImpl registry) {
    SceneDocument document = SceneDocument.parse(scenePath, json);
    for (SceneDocument.EntitySpec entitySpec : document.entities()) {
      Entity entity = world.createEntity(entitySpec.id(), EntityKind.of(entitySpec.kind()));
      EntityId entityId = entity.id();
      List<SceneDocument.ComponentSpec> components = entitySpec.components();
      for (SceneDocument.ComponentSpec componentSpec : components) {
        Component component =
            registry.deserialize(
                scenePath,
                entitySpec.id(),
                componentSpec.typeName(),
                new JsonComponentData(componentSpec.properties()));
        world.addComponent(entityId, component);
      }
      validateDrawableMaterial(scenePath, entitySpec.id(), components);
    }
    return document.renderPipeline();
  }

  private static void validateDrawableMaterial(
      String scenePath, String entityId, List<SceneDocument.ComponentSpec> components) {
    boolean hasDrawable = false;
    boolean hasMaterial = false;
    String drawableType = null;
    for (SceneDocument.ComponentSpec spec : components) {
      if (BuiltinComponents.SPRITE.equals(spec.typeName())
          || BuiltinComponents.MESH.equals(spec.typeName())) {
        hasDrawable = true;
        drawableType = spec.typeName();
      }
      if (BuiltinComponents.MATERIAL.equals(spec.typeName())) {
        hasMaterial = true;
      }
    }
    if (hasDrawable && !hasMaterial) {
      String entityLabel = entityId == null || entityId.isBlank() ? "<unnamed>" : "'" + entityId + "'";
      throw new SceneParseException(
          "Scene '"
              + scenePath
              + "': entity "
              + entityLabel
              + ": "
              + drawableType
              + " requires a Material component");
    }
  }
}
