package dev.hermes.core.ecs;

import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneRegistry;
import dev.hermes.core.scene.AssetSceneSource;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory scene definition registry. */
final class SceneRegistryImpl implements SceneRegistry {

  private final ComponentRegistryImpl registry;
  private final Map<String, SceneDefinition> definitions = new LinkedHashMap<>();

  SceneRegistryImpl(ComponentRegistryImpl registry) {
    this.registry = registry;
  }

  @Override
  public void register(SceneDefinition definition) {
    if (definitions.containsKey(definition.id())) {
      throw new IllegalArgumentException("Scene '" + definition.id() + "' is already registered");
    }
    definitions.put(definition.id(), definition);
  }

  @Override
  public void register(String id, String assetPath) {
    register(new SceneDefinition(id, new AssetSceneSource(assetPath)));
  }

  SceneDefinition get(String id) {
    return definitions.get(id);
  }
}
