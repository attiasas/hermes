package dev.hermes.core.resource;

import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.resource.LoadProgress;
import dev.hermes.api.resource.LoadTicket;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.api.resource.ResourceService;
import dev.hermes.core.HermesAssetPaths;

import java.util.Objects;
import java.util.Optional;

/** Central resource orchestrator: resolve, sync load, cache, and lifecycle. */
public final class ResourceManagerImpl implements ResourceService {

    private static final String DEFAULT_PROFILE_BASE = "resources/";

    private final ResourceCatalog catalog;
    private final ResourcePathResolver resolver;
    private final ResourceLoaderRegistry registry;
    private final ResourceCache cache;
    private final SyncLoadPipeline syncPipeline;
    private final String bundlesDirectory;

    private ResourceManagerImpl(
            ResourceCatalog catalog,
            String bundlesDirectory,
            ResourceLoaderRegistry registry) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.bundlesDirectory = normalizeBase(bundlesDirectory);
        this.registry = Objects.requireNonNull(registry, "registry");
        this.resolver = new ResourcePathResolver(catalog);
        this.cache = new ResourceCache();
        this.syncPipeline = new SyncLoadPipeline(cache, registry);
    }

    public static ResourceManagerImpl createDefault() {
        return forProfileBase(DEFAULT_PROFILE_BASE);
    }

    public static ResourceManagerImpl forTest(String profileBase) {
        return forProfileBase(profileBase);
    }

    private static ResourceManagerImpl forProfileBase(String profileBase) {
        String base = normalizeBase(profileBase);
        ResourceCatalog catalog = loadCatalogOrEmpty(base + "catalog.json");
        return new ResourceManagerImpl(catalog, base + "bundles", ResourceLoaderRegistry.withDefaults());
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

    ResourceCache cache() {
        return cache;
    }

    @Override
    public ResourceRef resolve(String pathOrAlias) {
        return ResourceRef.of(pathOrAlias);
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
        throw new UnsupportedOperationException("Async loading is not implemented yet");
    }

    @Override
    public LoadTicket loadBundleAsync(String bundleId) {
        throw new UnsupportedOperationException("Async loading is not implemented yet");
    }

    @Override
    public Optional<LoadProgress> activeProgress() {
        return Optional.empty();
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
        // Cooperative async loads are implemented in Task 10.
    }

    public void dispose() {
        cache.dispose();
    }

    private static String sceneGroupId(String sceneId) {
        return "scene:" + sceneId;
    }
}
