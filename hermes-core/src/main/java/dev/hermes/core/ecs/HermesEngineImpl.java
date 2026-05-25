package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.config.RuntimeConfigService;
import dev.hermes.core.config.RuntimeConfigServices;
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

    private final SceneManagerImpl sceneManager;
    private final ComponentRegistryImpl registry;
    private final ViewportServiceImpl viewport = new ViewportServiceImpl();
    private final List<SystemEntry> systems = new ArrayList<>();

    public HermesEngineImpl() {
        this.registry = new ComponentRegistryImpl();
        this.sceneManager = new SceneManagerImpl(registry);
        BuiltinComponents.register(registry);
        loadServiceRegistrations();
    }

    public void bindApplication(dev.hermes.api.HermesApplication application) {
        sceneManager.bind(this, application.createSession());
    }

    private void loadServiceRegistrations() {
        for (dev.hermes.api.ecs.ComponentRegistration registration :
                ServiceLoader.load(dev.hermes.api.ecs.ComponentRegistration.class)) {
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
    public void addSystem(System system) {
        addSystem(system, SystemScope.GLOBAL);
    }

    @Override
    public void addSystem(System system, SystemScope scope) {
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