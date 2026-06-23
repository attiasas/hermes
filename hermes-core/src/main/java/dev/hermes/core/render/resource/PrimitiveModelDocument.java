package dev.hermes.core.render.resource;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Parsed procedural mesh generator JSON or synthetic {@code primitive:…} cache key. */
public final class PrimitiveModelDocument {

    private final String generator;
    private final float width;
    private final float height;
    private final float depth;
    private final float radius;
    private final int segments;

    private PrimitiveModelDocument(
            String generator,
            float width,
            float height,
            float depth,
            float radius,
            int segments) {
        this.generator = generator;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.radius = radius;
        this.segments = segments;
    }

    public String generator() {
        return generator;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float depth() {
        return depth;
    }

    public float radius() {
        return radius;
    }

    public int segments() {
        return segments;
    }

    public static boolean isSyntheticPath(String path) {
        return path != null && path.startsWith("primitive:");
    }

    public static String syntheticPath(String primitive, float[] size) {
        String type = primitive == null ? "" : primitive.trim().toLowerCase();
        switch (type) {
            case "box":
                return "primitive:box:"
                        + dim(size, 0, 1f)
                        + ":"
                        + dim(size, 1, 1f)
                        + ":"
                        + dim(size, 2, 1f);
            case "plane":
                return "primitive:plane:" + dim(size, 0, 1f) + ":" + dim(size, 1, 1f);
            case "sphere":
                return "primitive:sphere:" + dim(size, 0, 0.5f) + ":" + segmentCount(size, 1, 16);
            default:
                throw new IllegalArgumentException("unknown primitive: " + primitive);
        }
    }

    public static PrimitiveModelDocument parseJson(String json) {
        JsonValue root = new JsonReader().parse(json);
        if (!root.has("generator")) {
            throw new IllegalArgumentException("model JSON missing generator key");
        }
        int version = root.getInt("version", 1);
        if (version != 1) {
            throw new IllegalArgumentException("unsupported primitive model version: " + version);
        }
        String generator = root.getString("generator", "").trim().toLowerCase();
        return new PrimitiveModelDocument(
                generator,
                root.getFloat("width", 1f),
                root.getFloat("height", 1f),
                root.getFloat("depth", 1f),
                root.getFloat("radius", 0.5f),
                root.getInt("segments", 16));
    }

    public static PrimitiveModelDocument parseSyntheticPath(String path) {
        if (!isSyntheticPath(path)) {
            throw new IllegalArgumentException("not a primitive path: " + path);
        }
        String[] parts = path.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("invalid primitive path: " + path);
        }
        String generator = parts[1].trim().toLowerCase();
        switch (generator) {
            case "box":
                requireParts(parts, 5, path);
                return new PrimitiveModelDocument(
                        generator,
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3]),
                        Float.parseFloat(parts[4]),
                        0.5f,
                        16);
            case "plane":
                requireParts(parts, 4, path);
                return new PrimitiveModelDocument(
                        generator,
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3]),
                        1f,
                        0.5f,
                        16);
            case "sphere":
                requireParts(parts, 4, path);
                return new PrimitiveModelDocument(
                        generator,
                        1f,
                        1f,
                        1f,
                        Float.parseFloat(parts[2]),
                        Integer.parseInt(parts[3]));
            default:
                throw new IllegalArgumentException("unknown primitive generator: " + generator);
        }
    }

    private static float dim(float[] size, int index, float fallback) {
        return size != null && size.length > index ? size[index] : fallback;
    }

    private static int segmentCount(float[] size, int index, int fallback) {
        return size != null && size.length > index ? (int) size[index] : fallback;
    }

    private static void requireParts(String[] parts, int expected, String path) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("invalid primitive path: " + path);
        }
    }
}
