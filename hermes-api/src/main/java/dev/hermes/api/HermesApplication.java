package dev.hermes.api;

/**
 * Application lifecycle for Hermes games. Mirrors the engine tick order without exposing libGDX types.
 */
public interface HermesApplication {

  void create();

  void resize(int width, int height);

  void render();

  void pause();

  void resume();

  void dispose();
}
