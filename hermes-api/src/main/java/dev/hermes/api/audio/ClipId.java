package dev.hermes.api.audio;

import java.util.Objects;

/** Logical clip name resolved via {@code audio/profile.json} clips map. */
public final class ClipId {

    private final String id;

    private ClipId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("clip id is required");
        }
        this.id = id;
    }

    public static ClipId of(String id) {
        return new ClipId(id);
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClipId clipId = (ClipId) o;
        return id.equals(clipId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClipId{" + id + '}';
    }
}
