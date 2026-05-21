package dev.hermes.api;

import dev.hermes.api.ecs.HermesEngine;

/**
 * Application lifecycle for Hermes games. Mirrors the engine tick order without exposing libGDX types.
 *
 * <p>Bootstrap order: engine created → scene definitions registered → {@link #onCreate(HermesEngine)}
 * (register components/systems, spawn entities) → pending scene requests processed → render systems attached →
 * first {@link #resize(int, int)}.
 */
public interface HermesApplication {

  /**
   * Called once before pending scene requests are processed. Register custom components and systems, and create
   * entities in code here.
   */
  void onCreate(HermesEngine engine);

  /** Creates the session shared across scenes; override when save/audio state is needed. */
  default HermesSession createSession() {
    return HermesSession.EMPTY;
  }

  void resize(int width, int height);

  void render();

  void pause();

  void resume();

  void dispose();
}
