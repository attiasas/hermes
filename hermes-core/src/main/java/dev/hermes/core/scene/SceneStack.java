package dev.hermes.core.scene;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.resource.LoadTicket;
import dev.hermes.api.scene.SceneContext;
import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneLifecycle;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.EntityTypeRegistryImpl;
import dev.hermes.core.ecs.SceneLoadMetadata;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.SceneRegistryImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.lighting.LightingBudgetResolver;
import dev.hermes.core.lighting.LightingRuntime;
import dev.hermes.core.render.RenderPipelineExecutor;
import dev.hermes.core.resource.LoadingScreenController;
import dev.hermes.core.resource.ScenePreloadSpec;

import java.nio.charset.StandardCharsets;
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
    private LoadingScreenController loadingScreen;
    private PendingTransition pendingTransition;
    private String projectDefaultPipelinePath = "render/pipeline.json";

    public SceneStack(SceneRegistryImpl sceneRegistry) {
        this.sceneRegistry = Objects.requireNonNull(sceneRegistry, "sceneRegistry");
        this.componentRegistry = sceneRegistry.componentRegistry();
    }

    public void bind(HermesEngine engine, HermesSession session, LoadingScreenController loadingScreen) {
        this.engine = engine;
        this.session = session == null ? HermesSession.EMPTY : session;
        this.loadingScreen = loadingScreen;
        if (engine != null) {
            String pipeline = engine.runtimeConfig().gameRenderPipeline();
            if (pipeline != null && !pipeline.isBlank()) {
                projectDefaultPipelinePath = pipeline;
            }
        }
    }

    public boolean hasPendingTransition() {
        return pendingTransition != null;
    }

    /**
     * Polls an in-flight async preload transition. Returns {@code true} while still waiting on resources.
     */
    public boolean tickAsyncTransition() {
        if (pendingTransition == null) {
            return false;
        }
        PendingTransition pending = pendingTransition;
        LoadTicket ticket = pending.ticket;
        if (ticket != null && !ticket.done() && !ticket.failed()) {
            return true;
        }
        if (ticket != null && ticket.failed()) {
            ticket.error()
                    .ifPresent(
                            error ->
                                    log.error(
                                            "Async scene transition to '"
                                                    + pending.sceneId
                                                    + "' failed",
                                            error));
            endLoadingScreen();
            pendingTransition = null;
            return false;
        }
        if (pending.bundleIndex + 1 < pending.bundles.size()) {
            pending.bundleIndex++;
            pending.ticket = startBundleLoad(pending.bundles.get(pending.bundleIndex));
            return true;
        }
        completePendingTransition(pending);
        pendingTransition = null;
        return false;
    }

    public void setProjectDefaultPipelinePath(String projectDefaultPipelinePath) {
        if (projectDefaultPipelinePath != null && !projectDefaultPipelinePath.isBlank()) {
            this.projectDefaultPipelinePath = projectDefaultPipelinePath;
        }
    }

    public void goTo(String sceneId) {
        if (beginAsyncTransitionIfNeeded(PendingTransition.Kind.GO_TO, sceneId)) {
            return;
        }
        goToSync(sceneId);
    }

    public void push(String sceneId) {
        if (beginAsyncTransitionIfNeeded(PendingTransition.Kind.PUSH, sceneId)) {
            return;
        }
        pushSync(sceneId);
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
        SceneInstance instance =
                new SceneInstance(
                        definition.id(),
                        manager,
                        definition,
                        jsonMetadata.renderPipeline(),
                        jsonMetadata.inputContext(),
                        jsonMetadata.uiConfig(),
                        jsonMetadata.audioConfig(),
                        false);
        String pipelinePath =
                RenderPipelineExecutor.resolvePipelinePath(instance, projectDefaultPipelinePath);
        LightingBudgetResolver.apply(manager, pipelinePath);
        return instance;
    }

    private void enterScene(SceneInstance instance) {
        log.debug("Entering scene: " + instance.id());
        if (engine != null) {
            engine.ui().onSceneEnter(instance.id(), instance.uiConfig());
            engine.audio().onSceneEnter(instance.id(), instance.audioConfig());
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
            engine.audio().onSceneExit(instance.id());
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
        if (engine != null) {
            engine.audio().onScenePause(instance.id());
        }
        SceneLifecycle lifecycle = instance.definition().lifecycle();
        if (lifecycle != null) {
            lifecycle.onPause(sceneContext(instance));
        }
    }

    private void resumeScene(SceneInstance instance) {
        log.debug("Resuming scene: " + instance.id());
        instance.setPaused(false);
        if (engine != null) {
            engine.audio().onSceneResume(instance.id());
        }
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

    private void goToSync(String sceneId) {
        while (!stack.isEmpty()) {
            exitScene(stack.pop());
        }
        SceneDefinition definition = requireDefinition(sceneId);
        SceneInstance instance = loadScene(definition);
        stack.push(instance);
        enterScene(instance);
    }

    private void pushSync(String sceneId) {
        SceneInstance current = stack.peek();
        if (current != null) {
            pauseScene(current);
        }
        SceneDefinition definition = requireDefinition(sceneId);
        SceneInstance instance = loadScene(definition);
        stack.push(instance);
        enterScene(instance);
    }

    private boolean beginAsyncTransitionIfNeeded(PendingTransition.Kind kind, String sceneId) {
        SceneDefinition definition = requireDefinition(sceneId);
        Optional<ScenePreloadSpec> preload = readPreloadSpec(definition);
        if (preload.isEmpty() || !preload.get().async() || preload.get().bundles().isEmpty()) {
            return false;
        }
        if (engine == null) {
            return false;
        }
        ScenePreloadSpec spec = preload.get();
        if (kind == PendingTransition.Kind.GO_TO) {
            while (!stack.isEmpty()) {
                exitScene(stack.pop());
            }
        } else {
            SceneInstance current = stack.peek();
            if (current != null) {
                pauseScene(current);
            }
        }
        pendingTransition =
                new PendingTransition(kind, sceneId, definition, List.copyOf(spec.bundles()));
        pendingTransition.ticket = startBundleLoad(pendingTransition.bundles.get(0));
        if (spec.showLoadingScreen() && loadingScreen != null) {
            loadingScreen.begin(() -> aggregateProgress(pendingTransition), "Loading " + sceneId + "...");
        }
        return true;
    }

    private LoadTicket startBundleLoad(String bundleId) {
        return engine.resources().loadBundleAsync(bundleId);
    }

    private float aggregateProgress(PendingTransition pending) {
        int total = pending.bundles.size();
        if (total == 0) {
            return 1f;
        }
        float completed = pending.bundleIndex;
        if (pending.ticket != null) {
            completed += pending.ticket.progress();
        }
        return completed / total;
    }

    private void completePendingTransition(PendingTransition pending) {
        endLoadingScreen();
        SceneInstance instance = loadScene(pending.definition);
        stack.push(instance);
        enterScene(instance);
    }

    private void endLoadingScreen() {
        if (loadingScreen != null) {
            loadingScreen.end();
        }
    }

    private Optional<ScenePreloadSpec> readPreloadSpec(SceneDefinition definition) {
        if (!(definition.source() instanceof AssetSceneSource)) {
            return Optional.empty();
        }
        String assetPath = ((AssetSceneSource) definition.source()).assetPath();
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            return Optional.empty();
        }
        String json = handle.readString(StandardCharsets.UTF_8.name());
        return SceneLoader.loadMetadataFromString(json).preload();
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

    static final class PendingTransition {

        enum Kind {
            GO_TO,
            PUSH
        }

        final Kind kind;
        final String sceneId;
        final SceneDefinition definition;
        final List<String> bundles;
        int bundleIndex;
        LoadTicket ticket;

        PendingTransition(Kind kind, String sceneId, SceneDefinition definition, List<String> bundles) {
            this.kind = kind;
            this.sceneId = sceneId;
            this.definition = definition;
            this.bundles = bundles;
        }
    }
}
