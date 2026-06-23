package dev.hermes.core.render.resource;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

/** Procedural mesh builders for box, plane, and sphere primitives. */
public final class PrimitiveModelGenerator {

    private final ModelBuilder builder = new ModelBuilder();

    public Model box(float width, float height, float depth) {
        Material material = new Material(ColorAttribute.createDiffuse(1f, 1f, 1f, 1f));
        long attributes = Usage.Position | Usage.Normal;
        return builder.createBox(width, height, depth, material, attributes);
    }

    /** XZ plane with Y-up, centered at the origin. */
    public Model plane(float width, float height) {
        Material material = new Material(ColorAttribute.createDiffuse(1f, 1f, 1f, 1f));
        long attributes = Usage.Position | Usage.Normal;
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        return builder.createRect(
                -halfWidth, 0f, -halfHeight,
                halfWidth, 0f, -halfHeight,
                halfWidth, 0f, halfHeight,
                -halfWidth, 0f, halfHeight,
                0f, 1f, 0f,
                material,
                attributes);
    }

    public Model sphere(float radius, int segments) {
        Material material = new Material(ColorAttribute.createDiffuse(1f, 1f, 1f, 1f));
        long attributes = Usage.Position | Usage.Normal;
        return builder.createSphere(radius, radius, radius, segments, segments, material, attributes);
    }

    public Model generate(PrimitiveModelDocument document) {
        switch (document.generator()) {
            case "box":
                return box(document.width(), document.height(), document.depth());
            case "plane":
                return plane(document.width(), document.height());
            case "sphere":
                return sphere(document.radius(), document.segments());
            default:
                throw new IllegalArgumentException("unknown primitive generator: " + document.generator());
        }
    }
}
