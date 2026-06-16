package dev.hermes.api.resource;

import java.util.Objects;

public final class ResourceRef {
    private final String raw;

    private ResourceRef(String raw) {
        this.raw = Objects.requireNonNull(raw, "raw");
    }

    public static ResourceRef of(String pathOrAlias) {
        if (pathOrAlias == null || pathOrAlias.isBlank()) {
            throw new IllegalArgumentException("pathOrAlias must not be blank");
        }
        String trimmed = pathOrAlias.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return new ResourceRef(trimmed);
    }

    public String raw() {
        return raw;
    }

    public String path() {
        return raw;
    }

    public boolean alias() {
        return raw.startsWith("@");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceRef)) return false;
        return raw.equals(((ResourceRef) o).raw);
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    @Override
    public String toString() {
        return "ResourceRef{" + raw + "}";
    }
}
