package dev.hermes.core.ecs;

import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneManager;
import dev.hermes.api.scene.SceneRegistry;
import dev.hermes.core.scene.SceneInstance;
import dev.hermes.core.scene.SceneStack;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Scene manager delegating stack transitions to {@link SceneStack} and draining a request queue. */
public final class SceneManagerImpl implements SceneManager {

  private final ComponentRegistryImpl registry;
  private final SceneRegistryImpl sceneRegistry;
  private final SceneStack stack;
  private final Deque<SceneChangeRequest> pending = new ArrayDeque<>();
  private HermesEngine engine;
  private HermesSession session = HermesSession.EMPTY;

  public SceneManagerImpl(ComponentRegistryImpl registry) {
    this.registry = registry;
    this.sceneRegistry = new SceneRegistryImpl(registry);
    this.stack = new SceneStack(sceneRegistry);
  }

  void bind(HermesEngine engine, HermesSession session) {
    this.engine = engine;
    this.session = session == null ? HermesSession.EMPTY : session;
    stack.bind(engine, session);
  }

  @Override
  public void request(SceneChangeRequest request) {
    pending.addLast(Objects.requireNonNull(request, "request"));
  }

  @Override
  public void processPending() {
    while (!pending.isEmpty()) {
      SceneChangeRequest request = pending.removeFirst();
      switch (request.kind()) {
        case GO_TO:
          stack.goTo(request.sceneId());
          break;
        case PUSH:
          stack.push(request.sceneId());
          break;
        case POP:
          stack.pop();
          break;
        default:
          throw new IllegalStateException("Unhandled scene change kind: " + request.kind());
      }
    }
  }

  @Override
  public World activeWorld() {
    SceneInstance active = stack.active();
    return active != null ? active.world() : null;
  }

  @Override
  public SceneHandle active() {
    return stack.active();
  }

  @Override
  public List<SceneHandle> visibleScenes() {
    List<SceneHandle> scenes = new ArrayList<>();
    for (SceneInstance instance : stack.visibleScenesBottomToTop()) {
      scenes.add(instance);
    }
    return List.copyOf(scenes);
  }

  @Override
  public SceneRegistry registry() {
    return sceneRegistry;
  }

  @Override
  public int stackDepth() {
    return stack.depth();
  }
}
