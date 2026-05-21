package dev.hermes.core.ecs;

import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneRegistry;
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
    definitions.put(definition.id(), definition);
  }

  @Override
  public void register(String id, String assetPath) {
    register(
        SceneDefinition.builder(id)
            .source(ctx -> SceneLoader.load(assetPath, ctx.world(), registry))
            .build());
  }

  SceneDefinition get(String id) {
    return definitions.get(id);
  }
}
