package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;

import java.util.Objects;

/** Cache identity: {@link ResourceRef} plus {@link ResourceKind}. */
public final class ResourceKey {

    private final ResourceRef ref;
    private final ResourceKind kind;

    public ResourceKey(ResourceRef ref, ResourceKind kind) {
        this.ref = Objects.requireNonNull(ref, "ref");
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    public ResourceRef ref() {
        return ref;
    }

    public ResourceKind kind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceKey)) {
            return false;
        }
        ResourceKey that = (ResourceKey) o;
        return ref.equals(that.ref) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, kind);
    }

    @Override
    public String toString() {
        return "ResourceKey{" + ref + ", " + kind + '}';
    }
}
