package dev.hermes.core.audio;

import dev.hermes.api.audio.AudioBus;
import dev.hermes.api.audio.AudioMixer;
import dev.hermes.api.audio.AudioService;
import dev.hermes.api.audio.BgmController;
import dev.hermes.api.audio.ClipId;
import dev.hermes.api.audio.PlayOptions;
import dev.hermes.api.audio.SoundHandle;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.scene.SceneAudioConfig;
import dev.hermes.api.log.Logger;
import dev.hermes.api.log.Logs;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.resource.ResourcePlatform;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Default {@link AudioService} using libGDX sound playback behind {@link SoundBackend}. */
public final class AudioServiceImpl implements AudioService {

    private static final Logger log = Logs.get(AudioServiceImpl.class);
    private static final int DEFAULT_MAX_INSTANCES_PER_CLIP = 8;

    private final SoundBackend backend;
    private final ResourceManagerImpl resources;
    private final BgmControllerImpl bgmController;
    private AudioMixer mixer;
    private AudioProfile profile;
    private boolean missingProfileLogged;
    private boolean htmlSoundSkipLogged;
    private final Map<String, SceneAudioConfig> sceneConfigs = new HashMap<>();
    private String bgmOwnerSceneId;
    private final Map<String, Deque<GdxSoundHandle>> activeByPath = new HashMap<>();
    private final Deque<GdxSoundHandle> allSfx = new ArrayDeque<>();
    private boolean instanceCapWarned;

    public AudioServiceImpl(SoundBackend backend, AudioMixer mixer, ResourceManagerImpl resources) {
        this(backend, new NoopMusicBackend(), mixer, resources);
    }

    AudioServiceImpl(
            SoundBackend backend, MusicBackend musicBackend, AudioMixer mixer, ResourceManagerImpl resources) {
        this.backend = backend;
        this.mixer = mixer;
        this.resources = resources;
        this.bgmController = new BgmControllerImpl(musicBackend, mixer);
    }

    public static AudioServiceImpl createDefault(
            AudioMixerImpl mixer, ResourceManagerImpl resources, SoundBackend soundBackend) {
        GdxMusicBackend musicBackend = new GdxMusicBackend();
        return new AudioServiceImpl(soundBackend, musicBackend, mixer, resources);
    }

    public void setMixer(AudioMixer mixer) {
        this.mixer = mixer;
    }

    public void dispose() {
        bgmController.dispose();
    }

    @Override
    public AudioMixer mixer() {
        return mixer;
    }

    @Override
    public SoundHandle play(String clipPath, PlayOptions options) {
        ensureSoundLoaded(clipPath);
        float gain = mixer.effectiveGain(options.bus()) * options.volume();
        long id = backend.play(clipPath, gain, options.pitch(), options.pan(), options.loop());
        if (options.worldX().isPresent()) {
            backend.setPosition(
                    clipPath,
                    id,
                    options.worldX().get(),
                    options.worldY().orElse(0f),
                    options.worldZ().orElse(0f));
        }
        GdxSoundHandle[] handleRef = new GdxSoundHandle[1];
        handleRef[0] =
                new GdxSoundHandle(backend, clipPath, id, gain, () -> untrack(clipPath, handleRef[0]));
        trackInstance(clipPath, handleRef[0]);
        return handleRef[0];
    }

    @Override
    public SoundHandle play(ClipId clipId, PlayOptions options) {
        if (profile != null) {
            Optional<String> path = profile.resolveClip(clipId.id());
            if (path.isPresent()) {
                return play(path.get(), options);
            }
        }
        return play(clipId.id(), options);
    }

    @Override
    public void stopAll(String clipPath) {
        if (clipPath == null) {
            for (GdxSoundHandle handle : new ArrayList<>(allSfx)) {
                handle.stop();
            }
            allSfx.clear();
            activeByPath.clear();
            return;
        }
        Deque<GdxSoundHandle> instances = activeByPath.remove(clipPath);
        if (instances == null) {
            return;
        }
        for (GdxSoundHandle handle : instances) {
            handle.stop();
            allSfx.remove(handle);
        }
    }

    @Override
    public BgmController bgm() {
        return bgmController;
    }

    @Override
    public void loadProfile(String profilePath) {
        if (profilePath == null || profilePath.isBlank()) {
            return;
        }
        if (!HermesAssetPaths.internal(profilePath).exists()) {
            if (!missingProfileLogged) {
                log.warn("Audio profile not found: " + profilePath);
                missingProfileLogged = true;
            }
            return;
        }
        profile = AudioProfileLoader.load(profilePath);
        applyProfileBusVolumes();
    }

    private void applyProfileBusVolumes() {
        for (AudioBus bus : AudioBus.values()) {
            mixer.setVolume(bus, profile.busVolume(bus));
        }
    }

    @Override
    public void tick(float delta, WorldManager activeManager, float surfaceWidth, float surfaceHeight) {
        if (activeManager != null) {
            AudioListenerUpdater.update(backend, activeManager, surfaceWidth, surfaceHeight);
        }
        bgmController.tick(delta);
    }

    @Override
    public void onSceneEnter(String sceneId, Optional<SceneAudioConfig> config) {
        config.ifPresent(c -> sceneConfigs.put(sceneId, c));
        if (!config.isPresent()) {
            return;
        }
        SceneAudioConfig audioConfig = config.get();
        Optional<String> playlistPath = resolvePlaylistPath(audioConfig);
        if (playlistPath.isPresent()) {
            bgmOwnerSceneId = sceneId;
            bgm().crossfadeTo(playlistPath.get(), audioConfig.fadeInSeconds());
        }
    }

    @Override
    public void onSceneExit(String sceneId) {
        SceneAudioConfig config = sceneConfigs.remove(sceneId);
        if (sceneId.equals(bgmOwnerSceneId)) {
            float fadeOut = config != null ? config.fadeOutSeconds() : 1f;
            bgm().stop(fadeOut);
            bgmOwnerSceneId = null;
        }
    }

    @Override
    public void onScenePause(String sceneId) {
        SceneAudioConfig config = sceneConfigs.get(sceneId);
        if (config != null && config.pauseBgmOnPause()) {
            bgm().pause();
        }
    }

    @Override
    public void onSceneResume(String sceneId) {
        SceneAudioConfig config = sceneConfigs.get(sceneId);
        if (config == null) {
            return;
        }
        if (config.pauseBgmOnPause()) {
            bgm().resume();
            return;
        }
        Optional<String> playlistPath = resolvePlaylistPath(config);
        if (playlistPath.isPresent() && !bgm().isPlaying()) {
            bgmOwnerSceneId = sceneId;
            bgm().crossfadeTo(playlistPath.get(), config.fadeInSeconds());
        }
    }

    private static Optional<String> resolvePlaylistPath(SceneAudioConfig config) {
        if (config.bgmPlaylistPath().isPresent()) {
            return config.bgmPlaylistPath();
        }
        if (config.bgmPlaylistId().isPresent()) {
            return Optional.of("audio/bgm/" + config.bgmPlaylistId().get() + ".json");
        }
        return Optional.empty();
    }

    AudioProfile currentProfile() {
        return profile;
    }

    void loadProfileFromJson(String json) {
        profile = AudioProfileLoader.parse(json);
        applyProfileBusVolumes();
    }

    private void ensureSoundLoaded(String clipPath) {
        if (clipPath == null || clipPath.isBlank()) {
            return;
        }
        if (ResourcePlatform.isHtmlPlatform()) {
            if (!htmlSoundSkipLogged) {
                log.debug("Skipping sound resource load on HTML platform");
                htmlSoundSkipLogged = true;
            }
            return;
        }
        resources.loadSync(ResourceRef.of(clipPath), ResourceKind.SOUND);
    }

    private void trackInstance(String clipPath, GdxSoundHandle handle) {
        activeByPath.computeIfAbsent(clipPath, key -> new ArrayDeque<>()).addLast(handle);
        allSfx.addLast(handle);
        enforceInstanceCap(clipPath);
    }

    private void untrack(String clipPath, GdxSoundHandle handle) {
        Deque<GdxSoundHandle> instances = activeByPath.get(clipPath);
        if (instances != null) {
            instances.remove(handle);
            if (instances.isEmpty()) {
                activeByPath.remove(clipPath);
            }
        }
        allSfx.remove(handle);
    }

    private void enforceInstanceCap(String clipPath) {
        int maxInstances =
                profile != null ? profile.maxInstancesPerClip() : DEFAULT_MAX_INSTANCES_PER_CLIP;
        Deque<GdxSoundHandle> instances = activeByPath.get(clipPath);
        if (instances == null) {
            return;
        }
        while (instances.size() > maxInstances) {
            GdxSoundHandle oldest = instances.peekFirst();
            if (oldest == null) {
                break;
            }
            if (!instanceCapWarned) {
                log.warn("Max sound instances per clip reached; stopping oldest instance");
                instanceCapWarned = true;
            }
            oldest.stop();
        }
    }

    private static final class NoopMusicBackend implements MusicBackend {

        @Override
        public MusicHandle load(String path) {
            return NoopMusicHandle.INSTANCE;
        }

        @Override
        public void disposeMusic() {}
    }

    private static final class NoopMusicHandle implements MusicHandle {

        private static final NoopMusicHandle INSTANCE = new NoopMusicHandle();

        @Override
        public void play(float volume, boolean loop) {}

        @Override
        public void stop() {}

        @Override
        public void setVolume(float volume) {}

        @Override
        public boolean isPlaying() {
            return false;
        }

        @Override
        public void pause() {}

        @Override
        public void resume() {}

        @Override
        public void dispose() {}
    }
}
