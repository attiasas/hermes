package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Parsed {@code resources/bundles/*.json} preload group. */
public final class ResourceBundle {

    private final int version;
    private final String id;
    private final List<Entry> resources;

    private ResourceBundle(int version, String id, List<Entry> resources) {
        this.version = version;
        this.id = id;
        this.resources = resources;
    }

    static ResourceBundle of(int version, String id, List<Entry> resources) {
        return new ResourceBundle(version, id, List.copyOf(resources));
    }

    public int version() {
        return version;
    }

    public String id() {
        return id;
    }

    public List<Entry> resources() {
        return resources;
    }

    public static final class Entry {
        private final ResourceRef ref;
        private final ResourceKind kind;

        public Entry(ResourceRef ref, ResourceKind kind) {
            this.ref = Objects.requireNonNull(ref, "ref");
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        public ResourceRef ref() {
            return ref;
        }

        public ResourceKind kind() {
            return kind;
        }
    }
}
