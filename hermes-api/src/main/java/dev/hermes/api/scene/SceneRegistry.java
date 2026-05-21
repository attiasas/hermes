package dev.hermes.api.scene;

/** Registers scene definitions available to {@link SceneManager}. */
public interface SceneRegistry {

  void register(SceneDefinition definition);

  /** Convenience registration that loads a JSON scene asset from {@code assetPath}. */
  void register(String id, String assetPath);
}
