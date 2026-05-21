package dev.hermes.api.scene;

/** Optional hooks invoked when a scene enters, exits, or changes pause state. */
public interface SceneLifecycle {

  default void onEnter(SceneContext ctx) {}

  default void onExit(SceneContext ctx) {}

  default void onPause(SceneContext ctx) {}

  default void onResume(SceneContext ctx) {}
}
