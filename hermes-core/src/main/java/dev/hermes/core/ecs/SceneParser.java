package dev.hermes.core.ecs;

import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.World;

/** Loads a scene document into a world using a component registry. */
final class SceneParser {

  private SceneParser() {}

  static void loadIntoWorld(
      String scenePath, String json, World world, ComponentRegistryImpl registry) {
    SceneDocument document = SceneDocument.parse(scenePath, json);
    for (SceneDocument.EntitySpec entitySpec : document.entities()) {
      Entity entity = world.createEntity(entitySpec.id());
      EntityId entityId = entity.id();
      for (SceneDocument.ComponentSpec componentSpec : entitySpec.components()) {
        Component component =
            registry.deserialize(
                scenePath,
                entitySpec.id(),
                componentSpec.typeName(),
                new JsonComponentData(componentSpec.properties()));
        world.addComponent(entityId, component);
      }
    }
  }
}
