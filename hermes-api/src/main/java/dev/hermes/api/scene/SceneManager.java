package dev.hermes.api.scene;

import dev.hermes.api.ecs.World;
import java.util.List;

/** Multi-scene stack: registration, queued transitions, and active scene access. */
public interface SceneManager {

  void request(SceneChangeRequest request);

  void processPending();

  World activeWorld();

  SceneHandle active();

  List<SceneHandle> visibleScenes();

  SceneRegistry registry();

  int stackDepth();
}
