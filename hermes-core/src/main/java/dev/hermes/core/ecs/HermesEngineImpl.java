package dev.hermes.core.ecs;

import dev.hermes.api.audio.AudioMixer;
import dev.hermes.api.audio.AudioService;
import dev.hermes.api.animation.AnimationRegistration;
import dev.hermes.api.animation.AnimationRegistrar;
import dev.hermes.api.animation.AnimationService;
import dev.hermes.api.animation.AnimationTrackResolver;
import dev.hermes.api.ecs.ComponentRegistry;
import dev.hermes.api.ecs.EntityTypeRegistry;
import dev.hermes.api.ecs.HermesEngine;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.SystemScope;
import dev.hermes.api.config.RuntimeConfigService;
import dev.hermes.api.input.InputService;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.core.config.RuntimeConfigServices;
import dev.hermes.core.audio.AudioBackends;
import dev.hermes.core.audio.AudioMixerImpl;
import dev.hermes.core.audio.AudioServiceImpl;
import dev.hermes.core.audio.SoundBackend;
import dev.hermes.core.input.InputServiceImpl;
import dev.hermes.core.ui.UiServiceImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;
import dev.hermes.api.ui.UiService;
import dev.hermes.api.ui.UiWidgetRegistration;
import dev.hermes.api.resource.ResourceLoaderRegistration;
import dev.hermes.api.world.SpatialIndexRegistration;
import dev.hermes.api.resource.ResourceService;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.animation.AnimationBackendRegistry;
import dev.hermes.core.animation.AnimationServiceImpl;
import dev.hermes.core.animation.AnimationTargetApplier;
import dev.hermes.core.animation.AnimationTrackEvaluator;
import dev.hermes.core.animation.HermesTrackBackend;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.world.SpatialIndexRegistrations;

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
    private final EntityTypeRegistryImpl entityTypes = new EntityTypeRegistryImpl();
    private final ViewportServiceImpl viewport = new ViewportServiceImpl();
    private final InputServiceImpl input;
    private final SoundBackend soundBackend = AudioBackends.gdx();
    private final ResourceManagerImpl resources = ResourceManagerImpl.createDefault(soundBackend);
    private final AnimationBackendRegistry animationBackends = new AnimationBackendRegistry();
    private final List<AnimationTrackResolver> animationTrackResolvers = new ArrayList<>();
    private final UiServiceImpl ui;
    private final AudioMixerImpl internalMixer = new AudioMixerImpl();
    private final AudioServiceImpl audio;
    private final AnimationServiceImpl animation;
    private final List<SystemEntry> systems = new ArrayList<>();

    public HermesEngineImpl() {
        this.registry = new ComponentRegistryImpl();
        this.registry.setResources(resources);
        this.sceneManager = new SceneManagerImpl(registry);
        this.animation = new AnimationServiceImpl(sceneManager);
        this.input = new InputServiceImpl(this);
        this.ui = new UiServiceImpl(resources);
        this.audio = AudioServiceImpl.createDefault(internalMixer, resources, soundBackend);
        animationBackends.register(
                new HermesTrackBackend(
                        new AnimationTrackEvaluator(),
                        new AnimationTargetApplier(animationTrackResolvers)));
        BuiltinComponents.register(registry);
        BuiltinComponents.registerSystems(this);
        loadServiceRegistrations();
    }

    public void bindApplication(dev.hermes.api.HermesApplication application) {
        dev.hermes.api.HermesSession session = application.createSession();
        AudioMixer sessionMixer = session.mixer();
        if (sessionMixer != AudioMixer.NOOP) {
            audio.setMixer(sessionMixer);
        }
        sceneManager.bind(this, session);
    }

    private void loadServiceRegistrations() {
        for (dev.hermes.api.ecs.ComponentRegistration registration :
                ServiceLoader.load(dev.hermes.api.ecs.ComponentRegistration.class)) {
            log.debug("Loading service registration: " + registration.getClass().getName());
            registration.register(this);
        }
        for (UiWidgetRegistration registration : ServiceLoader.load(UiWidgetRegistration.class)) {
            log.debug("Loading UI widget registration: " + registration.getClass().getName());
            registration.register(ui.widgets());
        }
        for (ResourceLoaderRegistration registration : ServiceLoader.load(ResourceLoaderRegistration.class)) {
            log.debug("Loading resource loader registration: " + registration.getClass().getName());
            registration.register(resources.loaderRegistry());
        }
        for (SpatialIndexRegistration registration : ServiceLoader.load(SpatialIndexRegistration.class)) {
            log.debug("Loading spatial index registration: " + registration.getClass().getName());
            registration.register(SpatialIndexRegistrations.registry());
        }
        AnimationRegistrar animationRegistrar =
                new AnimationRegistrar() {
                    @Override
                    public void trackResolver(AnimationTrackResolver resolver) {
                        if (resolver != null) {
                            animationTrackResolvers.add(resolver);
                        }
                    }

                    @Override
                    public void backend(Object backend) {
                        if (!(backend instanceof dev.hermes.core.animation.AnimationBackend)) {
                            throw new IllegalArgumentException(
                                    "Animation backend must be instance of core AnimationBackend");
                        }
                        animationBackends.register((dev.hermes.core.animation.AnimationBackend) backend);
                    }
                };
        for (AnimationRegistration registration : ServiceLoader.load(AnimationRegistration.class)) {
            log.debug("Loading animation registration: " + registration.getClass().getName());
            registration.register(this, animationRegistrar);
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
    public EntityTypeRegistry entityTypes() {
        return entityTypes;
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

    @Override
    public UiService ui() {
        return ui;
    }

    @Override
    public AudioService audio() {
        return audio;
    }

    @Override
    public ResourceService resources() {
        return resources;
    }

    @Override
    public AnimationService animation() {
        return animation;
    }

    public void dispose() {
        resources.dispose();
        audio.dispose();
    }

    public List<SystemEntry> systems() {
        return Collections.unmodifiableList(systems);
    }

    ComponentRegistryImpl registryImpl() {
        return registry;
    }

    AnimationBackendRegistry animationBackends() {
        return animationBackends;
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