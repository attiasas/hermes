package dev.hermes.core.scene;

import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.scene.SceneContext;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneLifecycle;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;
import dev.hermes.core.ecs.SceneLoadMetadata;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.SceneRegistryImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.lighting.LightingRuntime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stack of loaded scenes with go-to, push, and pop transitions.
 */
public final class SceneStack {

    private static final Logger log = Logs.get(SceneStack.class);

    private final SceneRegistryImpl sceneRegistry;
    private final ComponentRegistry componentRegistry;
    private final Deque<SceneInstance> stack = new ArrayDeque<>();
    private HermesEngine engine;
    private HermesSession session = HermesSession.EMPTY;

    public SceneStack(SceneRegistryImpl sceneRegistry) {
        this.sceneRegistry = Objects.requireNonNull(sceneRegistry, "sceneRegistry");
        this.componentRegistry = sceneRegistry.componentRegistry();
    }

    public void bind(HermesEngine engine, HermesSession session) {
        this.engine = engine;
        this.session = session == null ? HermesSession.EMPTY : session;
    }

    public void goTo(String sceneId) {
        while (!stack.isEmpty()) {
            exitScene(stack.pop());
        }
        SceneDefinition definition = requireDefinition(sceneId);
        SceneInstance instance = loadScene(definition);
        stack.push(instance);
        enterScene(instance);
    }

    public void push(String sceneId) {
        SceneInstance current = stack.peek();
        if (current != null) {
            pauseScene(current);
        }
        SceneDefinition definition = requireDefinition(sceneId);
        SceneInstance instance = loadScene(definition);
        stack.push(instance);
        enterScene(instance);
    }

    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop an empty scene stack");
        }
        SceneInstance exiting = stack.pop();
        exitScene(exiting);
        SceneInstance resumed = stack.peek();
        if (resumed != null) {
            resumeScene(resumed);
        }
    }

    public int depth() {
        return stack.size();
    }

    public SceneInstance active() {
        return stack.peek();
    }

    /**
     * Scenes from stack bottom (oldest) to top (newest/active).
     */
    public List<SceneInstance> visibleScenesBottomToTop() {
        List<SceneInstance> scenes = new ArrayList<>();
        Iterator<SceneInstance> iterator = stack.descendingIterator();
        while (iterator.hasNext()) {
            scenes.add(iterator.next());
        }
        return List.copyOf(scenes);
    }

    private SceneInstance loadScene(SceneDefinition definition) {
        log.debug("Loading scene: " + definition.id());
        EntityTypeRegistryImpl types =
                engine == null ? new EntityTypeRegistryImpl() : (EntityTypeRegistryImpl) engine.entityTypes();
        WorldManagerImpl manager = new WorldManagerImpl(types, (ComponentRegistryImpl) componentRegistry);
        SceneLoadContext ctx =
                new SceneLoadContext() {
                    @Override
                    public WorldManager manager() {
                        return manager;
                    }

                    @Override
                    public ComponentRegistry registry() {
                        return componentRegistry;
                    }
                };
        SceneLoadMetadata jsonMetadata = SceneLoadMetadata.empty();
        if (definition.source() instanceof AssetSceneSource) {
            jsonMetadata =
                    SceneLoader.load(
                            ((AssetSceneSource) definition.source()).assetPath(),
                            ctx,
                            engine == null ? new EntityTypeRegistryImpl() : engine.entityTypes());
        } else {
            definition.source().populate(ctx);
        }
        return new SceneInstance(
                definition.id(),
                manager,
                definition,
                jsonMetadata.renderPipeline(),
                jsonMetadata.inputContext(),
                jsonMetadata.uiConfig(),
                false);
    }

    private void enterScene(SceneInstance instance) {
        log.debug("Entering scene: " + instance.id());
        if (engine != null) {
            engine.ui().onSceneEnter(instance.id(), instance.uiConfig());
        }
        SceneLifecycle lifecycle = instance.definition().lifecycle();
        if (lifecycle != null) {
            lifecycle.onEnter(sceneContext(instance));
        }
    }

    private void exitScene(SceneInstance instance) {
        log.debug("Exiting scene: " + instance.id());
        if (engine != null) {
            engine.ui().onSceneExit(instance.id());
        }
        SceneLifecycle lifecycle = instance.definition().lifecycle();
        if (lifecycle != null) {
            lifecycle.onExit(sceneContext(instance));
        }
        LightingRuntime.remove(instance.manager().entities());
        instance.manager().entities().clear();
    }

    private void pauseScene(SceneInstance instance) {
        log.debug("Pausing scene: " + instance.id());
        instance.setPaused(true);
        SceneLifecycle lifecycle = instance.definition().lifecycle();
        if (lifecycle != null) {
            lifecycle.onPause(sceneContext(instance));
        }
    }

    private void resumeScene(SceneInstance instance) {
        log.debug("Resuming scene: " + instance.id());
        instance.setPaused(false);
        SceneLifecycle lifecycle = instance.definition().lifecycle();
        if (lifecycle != null) {
            lifecycle.onResume(sceneContext(instance));
        }
    }

    private SceneDefinition requireDefinition(String sceneId) {
        SceneDefinition definition = sceneRegistry.get(sceneId);
        if (definition == null) {
            throw new IllegalStateException("Scene '" + sceneId + "' is not registered");
        }
        return definition;
    }

    private SceneContext sceneContext(SceneInstance instance) {
        return new SceneContext() {
            @Override
            public String sceneId() {
                return instance.id();
            }

            @Override
            public WorldManager manager() {
                return instance.manager();
            }

            @Override
            public ComponentRegistry registry() {
                return componentRegistry;
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
