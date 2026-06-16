package dev.hermes.api.resource;

import java.util.Optional;

/** Central resource loading, caching, and lifecycle. */
public interface ResourceService {

    /** Resolve path or @alias to canonical ResourceRef. */
    ResourceRef resolve(String pathOrAlias);

    /** Returns true if ref is loaded and cached. */
    boolean isLoaded(ResourceRef ref, ResourceKind kind);

    /** Blocking load on calling thread. */
    void loadSync(ResourceRef ref, ResourceKind kind);

    /** Load every entry in a bundle synchronously. */
    void loadBundleSync(String bundleId);

    /** Start async load; returns ticket to poll. */
    LoadTicket loadAsync(ResourceRef ref, ResourceKind kind);

    LoadTicket loadBundleAsync(String bundleId);

    Optional<LoadProgress> activeProgress();

    void retain(ResourceRef ref, ResourceKind kind);

    void release(ResourceRef ref, ResourceKind kind);

    void retainSceneBundle(String sceneId, String bundleId);

    void releaseSceneResources(String sceneId);

    /** Advance cooperative async loads; call once per frame from the application loop. */
    void tick();
}
