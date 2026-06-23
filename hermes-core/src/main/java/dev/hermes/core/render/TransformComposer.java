package dev.hermes.core.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import dev.hermes.api.ecs.LocalTransform;
import dev.hermes.api.ecs.Transform;

/** Composes entity root {@link Transform} with part {@link LocalTransform} into a libGDX matrix. */
public final class TransformComposer {

    private TransformComposer() {}

    public static Matrix4 compose(Transform root, LocalTransform local) {
        Matrix4 matrix = new Matrix4();
        composeInto(matrix, root, local);
        return matrix;
    }

    public static void composeInto(Matrix4 target, Transform root, LocalTransform local) {
        target.idt();
        target.translate(
                root.x() + local.x(),
                root.y() + local.y(),
                root.z() + local.z());
        applyRotation(target, root.rotationX(), root.rotationY(), root.rotationZ());
        applyRotation(target, local.rotationX(), local.rotationY(), local.rotationZ());
        target.scale(
                root.scaleX() * local.scaleX(),
                root.scaleY() * local.scaleY(),
                root.scaleZ() * local.scaleZ());
    }

    private static void applyRotation(Matrix4 matrix, float rotationX, float rotationY, float rotationZ) {
        if (rotationX != 0f) {
            matrix.rotate(Vector3.X, rotationX);
        }
        if (rotationY != 0f) {
            matrix.rotate(Vector3.Y, rotationY);
        }
        if (rotationZ != 0f) {
            matrix.rotate(Vector3.Z, rotationZ);
        }
    }
}
