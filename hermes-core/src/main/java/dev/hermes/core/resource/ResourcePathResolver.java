package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoadException;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.HermesAssetPaths;

import java.util.Objects;

/** Resolves {@link ResourceRef} paths and catalog aliases to canonical asset paths. */
public final class ResourcePathResolver {

    private final ResourceCatalog catalog;

    public ResourcePathResolver(ResourceCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    public Resolved resolve(ResourceRef ref) {
        Objects.requireNonNull(ref, "ref");
        if (!ref.alias()) {
            throw new ResourceLoadException("Plain path requires explicit kind: " + ref.raw());
        }
        ResourceCatalog.Entry entry = catalog.resolve(ref);
        return resolvePath(entry.path(), entry.kind());
    }

    public Resolved resolve(ResourceRef ref, ResourceKind kind) {
        Objects.requireNonNull(ref, "ref");
        Objects.requireNonNull(kind, "kind");
        if (ref.alias()) {
            ResourceCatalog.Entry entry = catalog.resolve(ref);
            return resolvePath(entry.path(), entry.kind());
        }
        return resolvePath(ref.path(), kind);
    }

    private static Resolved resolvePath(String path, ResourceKind kind) {
        String assetPath = path;
        if (kind == ResourceKind.SPRITE_SHEET) {
            int hash = path.indexOf('#');
            if (hash > 0) {
                assetPath = path.substring(0, hash);
            }
        }
        if (!HermesAssetPaths.internal(assetPath).exists()) {
            throw new ResourceLoadException("Resource file not found: " + assetPath);
        }
        return new Resolved(path, kind);
    }

    public static final class Resolved {
        private final String path;
        private final ResourceKind kind;

        public Resolved(String path, ResourceKind kind) {
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
