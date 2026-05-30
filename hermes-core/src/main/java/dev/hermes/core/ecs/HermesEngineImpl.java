package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.config.RuntimeConfigService;
import dev.hermes.api.input.InputService;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.core.config.RuntimeConfigServices;
import dev.hermes.core.input.InputServiceImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;
import dev.hermes.api.viewport.ViewportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Default engine implementation wiring scene manager, registry, and systems.
 */
public final class HermesEngineImpl implements HermesEngine {

    private static final Logger log = Logs.get(HermesEngineImpl.class);

    private final SceneManagerImpl sceneManager;
    private final ComponentRegistryImpl registry;
    private final ViewportServiceImpl viewport = new ViewportServiceImpl();
    private final InputServiceImpl input;
    private final List<SystemEntry> systems = new ArrayList<>();

    public HermesEngineImpl() {
        this.registry = new ComponentRegistryImpl();
        this.sceneManager = new SceneManagerImpl(registry);
        this.input = new InputServiceImpl(this);
        BuiltinComponents.register(registry);
        BuiltinComponents.registerSystems(this);
        loadServiceRegistrations();
    }

    public void bindApplication(dev.hermes.api.HermesApplication application) {
        sceneManager.bind(this, application.createSession());
    }

    private void loadServiceRegistrations() {
        for (dev.hermes.api.ecs.ComponentRegistration registration :
                ServiceLoader.load(dev.hermes.api.ecs.ComponentRegistration.class)) {
            log.debug("Loading service registration: " + registration.getClass().getName());
            registration.register(this);
        }
    }

    @Override
    public SceneManagerImpl scenes() {
        return sceneManager;
    }

    @Override
    public ComponentRegistry registry() {
        return registry;
    }

    @Override
    public ViewportService viewport() {
        return viewport;
    }

    @Override
    public InputService input() {
        return input;
    }

    @Override
    public void addSystem(System system) {
        addSystem(system, SystemScope.GLOBAL);
    }

    @Override
    public void addSystem(System system, SystemScope scope) {
        log.debug("Adding system: " + system.getClass().getName() + " with scope: " + scope);
        systems.add(new SystemEntry(system, scope));
    }

    @Override
    public RuntimeConfigService runtimeConfig() {
        return RuntimeConfigServices.get();
    }

    public List<SystemEntry> systems() {
        return Collections.unmodifiableList(systems);
    }

    ComponentRegistryImpl registryImpl() {
        return registry;
    }

    public static final class SystemEntry {
        private final System system;
        private final SystemScope scope;

        public SystemEntry(System system, SystemScope scope) {
            this.system = system;
            this.scope = scope;
        }

        public System system() {
            return system;
        }

        public SystemScope scope() {
            return scope;
        }
    }
}