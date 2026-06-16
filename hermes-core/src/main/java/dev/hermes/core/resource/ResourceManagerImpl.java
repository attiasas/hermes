package dev.hermes.core.resource;

import dev.hermes.api.resource.LoadProgress;
import dev.hermes.api.resource.LoadTicket;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.api.resource.ResourceService;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.audio.AudioBackends;
import dev.hermes.core.audio.SoundBackend;

import com.badlogic.gdx.files.FileHandle;

import java.util.Objects;
import java.util.Optional;

/** Central resource orchestrator: resolve, sync/async load, cache, and lifecycle. */
public final class ResourceManagerImpl implements ResourceService {

    private static final String DEFAULT_PROFILE_BASE = "resources/";
    private static final String DEFAULT_PROFILE_PATH = "resources/profile.json";

    private ResourceCatalog catalog;
    private ResourcePathResolver resolver;
    private final ResourceLoaderRegistry registry;
    private final ResourceCache cache;
    private final SyncLoadPipeline syncPipeline;
    private AsyncLoadExecutor asyncExecutor;
    private String bundlesDirectory;
    private ResourceProfile profile;

    private boolean forceCooperativeForTests;

    private ResourceManagerImpl(
            ResourceProfile profile,
            ResourceCatalog catalog,
            String bundlesDirectory,
            ResourceLoaderRegistry registry) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.bundlesDirectory = normalizeBundlesDirectory(bundlesDirectory);
        this.registry = Objects.requireNonNull(registry, "registry");
        this.resolver = new ResourcePathResolver(catalog);
        this.cache = new ResourceCache();
        this.syncPipeline = new SyncLoadPipeline(cache, registry);
        this.asyncExecutor =
                new AsyncLoadExecutor(cache, registry, resolver, this::createLoadStrategy);
    }

    public static ResourceManagerImpl createDefault() {
        return createDefault(AudioBackends.gdx());
    }

    public static ResourceManagerImpl createDefault(SoundBackend soundBackend) {
        return forProfileBase(DEFAULT_PROFILE_BASE, soundBackend);
    }

    public static ResourceManagerImpl forTest(String profileBase) {
        return forProfileBase(profileBase, AudioBackends.gdx());
    }

    /** Test hook: force cooperative frame-sliced loading instead of the thread pool. */
    public void useCooperativeStrategyForTests(boolean cooperative) {
        forceCooperativeForTests = cooperative;
        asyncExecutor.resetStrategy();
    }

    private static ResourceManagerImpl forProfileBase(String profileBase, SoundBackend soundBackend) {
        String base = normalizeBase(profileBase);
        ResourceProfile profile = ResourceProfile.defaults();
        ResourceCatalog catalog = loadCatalogOrEmpty(profile.catalog());
        return new ResourceManagerImpl(
                profile, catalog, profile.bundlesDirectory(), ResourceLoaderRegistry.withDefaults(soundBackend));
    }

    /** Loads profile JSON and applies catalog + bundle directory settings. Idempotent. */
    public void loadProfile(String profilePath) {
        ResourceProfile loaded = resolveProfile(profilePath);
        this.profile = loaded;
        this.catalog = loadCatalogOrEmpty(loaded.catalog());
        this.bundlesDirectory = normalizeBundlesDirectory(loaded.bundlesDirectory());
        this.resolver = new ResourcePathResolver(catalog);
        asyncExecutor.shutdown();
        asyncExecutor = new AsyncLoadExecutor(cache, registry, resolver, this::createLoadStrategy);
    }

    public ResourceProfile profile() {
        return profile;
    }

    private static ResourceProfile resolveProfile(String profilePath) {
        String path = profilePath == null || profilePath.isBlank() ? DEFAULT_PROFILE_PATH : profilePath.trim();
        FileHandle handle = HermesAssetPaths.internal(path);
        if (!handle.exists()) {
            return ResourceProfile.defaults();
        }
        return ResourceProfileLoader.load(path);
    }

    private static ResourceCatalog loadCatalogOrEmpty(String catalogPath) {
        FileHandle handle = HermesAssetPaths.internal(catalogPath);
        if (!handle.exists()) {
            return ResourceCatalog.empty();
        }
        return ResourceCatalogLoader.load(catalogPath);
    }

    private static String normalizeBase(String profileBase) {
        String trimmed = Objects.requireNonNull(profileBase, "profileBase").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("profileBase must not be blank");
        }
        return trimmed.endsWith("/") ? trimmed : trimmed + "/";
    }

    private static String normalizeBundlesDirectory(String directory) {
        String trimmed = Objects.requireNonNull(directory, "bundlesDirectory").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("bundlesDirectory must not be blank");
        }
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private LoadExecutionStrategy createLoadStrategy() {
        if (forceCooperativeForTests || ResourcePlatform.isHtmlPlatform()) {
            return new CooperativeLoadStrategy(profile.cooperativeAssetsPerFrame());
        }
        return new ThreadPoolLoadStrategy();
    }

    ResourceCache cache() {
        return cache;
    }

    /** Loader registry for built-in and SPI-registered kinds. */
    public ResourceLoaderRegistry loaderRegistry() {
        return registry;
    }

    @Override
    public ResourceRef resolve(String pathOrAlias) {
        ResourceRef ref = ResourceRef.of(pathOrAlias);
        if (ref.alias()) {
            catalog.resolve(ref);
        }
        return ref;
    }

    @Override
    public boolean isLoaded(ResourceRef ref, ResourceKind kind) {
        return cache.contains(new ResourceKey(ref, kind));
    }

    @Override
    public void loadSync(ResourceRef ref, ResourceKind kind) {
        ResourcePathResolver.Resolved resolved = resolver.resolve(ref, kind);
        ResourceKey key = new ResourceKey(ref, resolved.kind());
        if (cache.contains(key)) {
            return;
        }
        syncPipeline.loadSync(key, resolved.path());
    }

    @Override
    public void loadBundleSync(String bundleId) {
        ResourceBundle bundle = ResourceBundleLoader.loadById(bundlesDirectory, bundleId);
        for (ResourceBundle.Entry entry : bundle.resources()) {
            loadSync(entry.ref(), entry.kind());
        }
    }

    @Override
    public LoadTicket loadAsync(ResourceRef ref, ResourceKind kind) {
        Objects.requireNonNull(ref, "ref");
        Objects.requireNonNull(kind, "kind");
        return asyncExecutor.load(ref, kind);
    }

    @Override
    public LoadTicket loadBundleAsync(String bundleId) {
        Objects.requireNonNull(bundleId, "bundleId");
        ResourceBundle bundle = ResourceBundleLoader.loadById(bundlesDirectory, bundleId);
        return asyncExecutor.loadBundle(bundleId, bundle.resources());
    }

    @Override
    public Optional<LoadProgress> activeProgress() {
        return asyncExecutor.activeProgress();
    }

    @Override
    public void retain(ResourceRef ref, ResourceKind kind) {
        cache.retain(new ResourceKey(ref, kind));
    }

    @Override
    public void release(ResourceRef ref, ResourceKind kind) {
        cache.release(new ResourceKey(ref, kind));
    }

    @Override
    public void retainSceneBundle(String sceneId, String bundleId) {
        Objects.requireNonNull(sceneId, "sceneId");
        Objects.requireNonNull(bundleId, "bundleId");
        ResourceBundle bundle = ResourceBundleLoader.loadById(bundlesDirectory, bundleId);
        String groupId = sceneGroupId(sceneId);
        for (ResourceBundle.Entry entry : bundle.resources()) {
            loadSync(entry.ref(), entry.kind());
            cache.retainGroup(groupId, new ResourceKey(entry.ref(), entry.kind()));
        }
    }

    @Override
    public void releaseSceneResources(String sceneId) {
        cache.releaseGroup(sceneGroupId(Objects.requireNonNull(sceneId, "sceneId")));
    }

    @Override
    public void tick() {
        asyncExecutor.tick();
    }

    public void dispose() {
        asyncExecutor.shutdown();
        cache.dispose();
    }

    private static String sceneGroupId(String sceneId) {
        return "scene:" + sceneId;
    }
}
