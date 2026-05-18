package dev.hermes.api;

import dev.hermes.api.ecs.HermesEngine;

/**
 * Application lifecycle for Hermes games. Mirrors the engine tick order without exposing libGDX types.
 */
public interface HermesApplication {

  /** Called after the engine is created; register custom components here before the scene loads. */
  default void onCreate(HermesEngine engine) {}

  void create();

  void resize(int width, int height);

  void render();

  void pause();

  void resume();

  void dispose();
}
