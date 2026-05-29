package dev.hermes.core.ecs;

import dev.hermes.api.HermesSession;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.World;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.scene.SceneChangeRequest;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneManager;
import dev.hermes.api.scene.SceneRegistry;
import dev.hermes.api.scene.SceneStackPolicy;
import dev.hermes.core.scene.SceneInstance;
import dev.hermes.core.scene.SceneStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Scene manager delegating stack transitions to {@link SceneStack} and draining a request queue.
 */
public final class SceneManagerImpl implements SceneManager {

    private static final Logger log = Logs.get(SceneManagerImpl.class);

    private final ComponentRegistryImpl registry;
    private final SceneRegistryImpl sceneRegistry;
    private final SceneStack stack;
    private final Deque<SceneChangeRequest> pending = new ArrayDeque<>();
    private SceneStackPolicy stackPolicy = SceneStackPolicy.defaults();
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
        log.debug("Requesting scene change: " + request.kind() + " for scene: " + request.sceneId());
        pending.addLast(Objects.requireNonNull(request, "request"));
    }

    @Override
    public void processPending() {
        while (!pending.isEmpty()) {
            SceneChangeRequest request = pending.removeFirst();
            log.debug("Processing scene change request: " + request.kind() + " for scene: " + request.sceneId());
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
        return scenesFor(stackPolicy.renderStackedScenes());
    }

    @Override
    public List<SceneHandle> updateScenes() {
        return scenesFor(stackPolicy.updateStackedScenes());
    }

    private List<SceneHandle> scenesFor(boolean includeStacked) {
        if (!includeStacked) {
            SceneInstance active = stack.active();
            return active == null ? List.of() : List.of(active);
        }
        List<SceneHandle> scenes = new ArrayList<>();
        for (SceneInstance instance : stack.visibleScenesBottomToTop()) {
            scenes.add(instance);
        }
        return List.copyOf(scenes);
    }

    @Override
    public void setStackPolicy(SceneStackPolicy policy) {
        this.stackPolicy = Objects.requireNonNull(policy, "policy");
    }

    @Override
    public SceneStackPolicy stackPolicy() {
        return stackPolicy;
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
