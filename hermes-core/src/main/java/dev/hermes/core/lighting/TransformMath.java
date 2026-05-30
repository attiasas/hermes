package dev.hermes.core.lighting;

import com.badlogic.gdx.math.Vector3;

import dev.hermes.api.ecs.Transform;

/** World-space position and direction helpers for light gathering. */
final class TransformMath {

    private TransformMath() {}

    /** World-space unit vector along entity local −Z after rotation (Hermes light aim convention). */
    static void worldNegZ(Transform transform, Vector3 out) {
        out.set(0f, 0f, -1f);
        if (transform.rotationX() != 0f) {
            out.rotate(Vector3.X, transform.rotationX());
        }
        if (transform.rotationY() != 0f) {
            out.rotate(Vector3.Y, transform.rotationY());
        }
        if (transform.rotationZ() != 0f) {
            out.rotate(Vector3.Z, transform.rotationZ());
        }
        out.nor();
    }

    static void worldPosition(Transform transform, Vector3 out) {
        out.set(transform.x(), transform.y(), transform.z());
    }
}
