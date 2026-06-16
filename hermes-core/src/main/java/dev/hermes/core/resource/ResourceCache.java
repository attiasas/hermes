package dev.hermes.core.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Reference-counted resource storage with scene-scoped release groups. */
public final class ResourceCache {

    private final Map<ResourceKey, ResourceCacheEntry> entries = new HashMap<>();
    private final Map<String, Set<ResourceKey>> groups = new HashMap<>();

    public void put(ResourceKey key, Object payload, Runnable onDispose) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(onDispose, "onDispose");
        if (entries.containsKey(key)) {
            throw new IllegalStateException("Resource already cached: " + key);
        }
        entries.put(key, new ResourceCacheEntry(payload, onDispose));
    }

    public void retain(ResourceKey key) {
        ResourceCacheEntry entry = requireEntry(key, "retain");
        entry.retain();
    }

    public void release(ResourceKey key) {
        ResourceCacheEntry entry = entries.get(key);
        if (entry == null) {
            return;
        }
        if (entry.release()) {
            entry.dispose();
            entries.remove(key);
            removeKeyFromGroups(key);
        }
    }

    public void retainGroup(String groupId, ResourceKey key) {
        Objects.requireNonNull(groupId, "groupId");
        Objects.requireNonNull(key, "key");
        Set<ResourceKey> group = groups.computeIfAbsent(groupId, id -> new HashSet<>());
        if (group.add(key)) {
            retain(key);
        }
    }

    public void releaseGroup(String groupId) {
        Objects.requireNonNull(groupId, "groupId");
        Set<ResourceKey> group = groups.remove(groupId);
        if (group == null) {
            return;
        }
        for (ResourceKey key : group) {
            release(key);
        }
    }

    public boolean contains(ResourceKey key) {
        return entries.containsKey(key);
    }

    public Object get(ResourceKey key) {
        ResourceCacheEntry entry = entries.get(key);
        return entry == null ? null : entry.payload();
    }

    public void dispose() {
        for (ResourceCacheEntry entry : entries.values()) {
            entry.dispose();
        }
        entries.clear();
        groups.clear();
    }

    private ResourceCacheEntry requireEntry(ResourceKey key, String operation) {
        Objects.requireNonNull(key, "key");
        ResourceCacheEntry entry = entries.get(key);
        if (entry == null) {
            throw new IllegalStateException("Cannot " + operation + " missing resource: " + key);
        }
        return entry;
    }

    private void removeKeyFromGroups(ResourceKey key) {
        for (Set<ResourceKey> group : groups.values()) {
            group.remove(key);
        }
    }
}
