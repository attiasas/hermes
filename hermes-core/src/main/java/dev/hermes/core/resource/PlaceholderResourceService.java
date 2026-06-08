package dev.hermes.core.resource;

import dev.hermes.api.resource.LoadProgress;
import dev.hermes.api.resource.LoadTicket;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.api.resource.ResourceService;

import java.util.Optional;

/** Temporary stub until {@code ResourceManagerImpl} is wired in Task 9. */
public final class PlaceholderResourceService implements ResourceService {

    private static final String MSG = "ResourceService not initialized";

    @Override
    public ResourceRef resolve(String pathOrAlias) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public boolean isLoaded(ResourceRef ref, ResourceKind kind) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void loadSync(ResourceRef ref, ResourceKind kind) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void loadBundleSync(String bundleId) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public LoadTicket loadAsync(ResourceRef ref, ResourceKind kind) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public LoadTicket loadBundleAsync(String bundleId) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public Optional<LoadProgress> activeProgress() {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void retain(ResourceRef ref, ResourceKind kind) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void release(ResourceRef ref, ResourceKind kind) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void retainSceneBundle(String sceneId, String bundleId) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void releaseSceneResources(String sceneId) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void tick() {
        throw new UnsupportedOperationException(MSG);
    }
}
