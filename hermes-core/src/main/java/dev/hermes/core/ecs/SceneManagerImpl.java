package dev.hermes.core.ecs;

import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneContext;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneLifecycle;
import dev.hermes.api.scene.SceneManager;
import dev.hermes.api.scene.SceneRegistry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Minimal scene manager for Task 2: single bootstrap world, {@link SceneChangeRequest.Kind#GO_TO} support, and
 * no-op {@link SceneChangeRequest.Kind#PUSH}/{@link SceneChangeRequest.Kind#POP} placeholders.
 */
public final class SceneManagerImpl implements SceneManager {

  private final ComponentRegistryImpl registry;
  private final SceneRegistryImpl sceneRegistry;
  private final WorldImpl bootstrapWorld;
  private final Deque<SceneHandleImpl> stack = new ArrayDeque<>();
  private SceneChangeRequest pending;
  private HermesEngine engine;
  private HermesSession session = HermesSession.EMPTY;

  public SceneManagerImpl(ComponentRegistryImpl registry) {
    this.registry = registry;
    this.sceneRegistry = new SceneRegistryImpl(registry);
    this.bootstrapWorld = new WorldImpl();
  }

  void bind(HermesEngine engine, HermesSession session) {
    this.engine = engine;
    this.session = session == null ? HermesSession.EMPTY : session;
  }

  @Override
  public void request(SceneChangeRequest request) {
    pending = Objects.requireNonNull(request, "request");
  }

  @Override
  public void processPending() {
    if (pending == null) {
      return;
    }
    SceneChangeRequest request = pending;
    pending = null;
    switch (request.kind()) {
      case GO_TO:
        goTo(request.sceneId());
        break;
      case PUSH:
        push(request.sceneId());
        break;
      case POP:
        pop();
        break;
      default:
        throw new IllegalStateException("Unhandled scene change kind: " + request.kind());
    }
  }

  @Override
  public World activeWorld() {
    SceneHandleImpl active = peekActive();
    return active != null ? active.world() : bootstrapWorld;
  }

  @Override
  public SceneHandle active() {
    return peekActive();
  }

  @Override
  public List<SceneHandle> visibleScenes() {
    return List.copyOf(stack);
  }

  @Override
  public SceneRegistry registry() {
    return sceneRegistry;
  }

  @Override
  public int stackDepth() {
    return stack.size();
  }

  private void goTo(String sceneId) {
    SceneDefinition definition = requireDefinition(sceneId);
    exitActiveScene();
    stack.clear();
    SceneHandleImpl handle = loadScene(definition);
    stack.push(handle);
    enterScene(handle, definition);
  }

  private void push(String sceneId) {
    // TODO(Task 5): pause current scene and push a new one onto the stack.
    SceneDefinition definition = requireDefinition(sceneId);
    SceneHandleImpl handle = loadScene(definition);
    stack.push(handle);
    enterScene(handle, definition);
  }

  private void pop() {
    // TODO(Task 5): exit and remove the top scene, resuming the previous one.
    if (stack.isEmpty()) {
      return;
    }
    SceneHandleImpl exiting = stack.pop();
    SceneDefinition definition = sceneRegistry.get(exiting.id());
    if (definition != null && definition.lifecycle() != null) {
      definition.lifecycle().onExit(sceneContext(exiting));
    }
  }

  private SceneHandleImpl loadScene(SceneDefinition definition) {
    // TODO(Task 5): allocate a dedicated world per stacked scene.
    SceneLoadContextImpl ctx = new SceneLoadContextImpl(bootstrapWorld, registry);
    definition.source().populate(ctx);
    return new SceneHandleImpl(definition.id(), bootstrapWorld);
  }

  private void enterScene(SceneHandleImpl handle, SceneDefinition definition) {
    SceneLifecycle lifecycle = definition.lifecycle();
    if (lifecycle != null) {
      lifecycle.onEnter(sceneContext(handle));
    }
  }

  private void exitActiveScene() {
    SceneHandleImpl active = peekActive();
    if (active == null) {
      return;
    }
    SceneDefinition definition = sceneRegistry.get(active.id());
    if (definition != null && definition.lifecycle() != null) {
      definition.lifecycle().onExit(sceneContext(active));
    }
  }

  private SceneDefinition requireDefinition(String sceneId) {
    SceneDefinition definition = sceneRegistry.get(sceneId);
    if (definition == null) {
      throw new IllegalStateException("Scene '" + sceneId + "' is not registered");
    }
    return definition;
  }

  private SceneHandleImpl peekActive() {
    return stack.peek();
  }

  private SceneContext sceneContext(SceneHandleImpl handle) {
    return new SceneContext() {
      @Override
      public String sceneId() {
        return handle.id();
      }

      @Override
      public World world() {
        return handle.world();
      }

      @Override
      public ComponentRegistry registry() {
        return registry;
      }

      @Override
      public HermesEngine engine() {
        return engine;
      }

      @Override
      public HermesSession session() {
        return session;
      }
    };
  }
}
