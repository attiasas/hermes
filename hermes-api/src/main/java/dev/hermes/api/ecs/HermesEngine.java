package dev.hermes.api.ecs;

/** Runtime engine context exposed to user applications during bootstrap. */
public interface HermesEngine {

  World world();

  ComponentRegistry registry();

  void addSystem(System system);

  void loadScene(String scenePath);
}
