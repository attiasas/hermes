package dev.hermes.core.render;

import com.badlogic.gdx.math.Matrix4;
import dev.hermes.api.ecs.LocalTransform;
import dev.hermes.api.ecs.Transform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TransformComposerTest {

    @Test
    void compose_appliesLocalOffset() {
        Transform root = new Transform(10f, 20f, 0f);
        LocalTransform local = new LocalTransform();
        local.setX(1f);
        Matrix4 m = TransformComposer.compose(root, local);
        assertEquals(11f, m.getTranslation(new com.badlogic.gdx.math.Vector3()).x, 0.001f);
    }
}
