package dev.hermes.api;

import dev.hermes.api.ecs.HermesEngine;

/**
 * Application lifecycle for Hermes games. Mirrors the engine tick order without exposing libGDX types.
 *
 * <p>Bootstrap order: engine created → {@link #onCreate(HermesEngine)} (register components/systems, spawn
 * entities) → scene JSON loaded → render systems attached → first {@link #resize(int, int)}.
 */
public interface HermesApplication {

  /**
   * Called once before the scene file is loaded. Register custom components and systems, and create entities in code
   * here.
   */
  void onCreate(HermesEngine engine);

  void resize(int width, int height);

  void render();

  void pause();

  void resume();

  void dispose();
}
