package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.api.resource.ResourceRef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Parsed {@code resources/catalog.json} alias map. */
public final class ResourceCatalog {

    private final int version;
    private final Map<String, Entry> entries;

    private ResourceCatalog(int version, Map<String, Entry> entries) {
        this.version = version;
        this.entries = entries;
    }

    public static ResourceCatalog empty() {
        return new ResourceCatalog(1, Collections.emptyMap());
    }

    static ResourceCatalog of(int version, Map<String, Entry> entries) {
        return new ResourceCatalog(version, Collections.unmodifiableMap(new HashMap<>(entries)));
    }

    public int version() {
        return version;
    }

    public Entry resolve(ResourceRef ref) {
        Objects.requireNonNull(ref, "ref");
        if (!ref.alias()) {
            throw new ResourceLoadException("Resource catalog lookup requires an alias ref: " + ref.raw());
        }
        Entry entry = entries.get(ref.raw());
        if (entry == null) {
            throw new ResourceLoadException("Unknown resource alias: " + ref.raw());
        }
        return entry;
    }

    public static final class Entry {
        private final String path;
        private final ResourceKind kind;

        public Entry(String path, ResourceKind kind) {
            this.path = Objects.requireNonNull(path, "path");
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        public String path() {
            return path;
        }

        public ResourceKind kind() {
            return kind;
        }
    }
}
