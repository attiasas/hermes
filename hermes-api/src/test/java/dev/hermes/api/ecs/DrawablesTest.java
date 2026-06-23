package dev.hermes.api.ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DrawablesTest {

    @Test
    void defaultPart_hasVisibleLocalTransform() {
        DrawablePart part = DrawablePart.mesh("body", "models/cube.obj");
        assertEquals(DrawableKind.MESH, part.kind());
        assertEquals("body", part.id());
        assertTrue(part.local().visible());
        assertEquals(0, part.local().spriteFrame());
    }

    @Test
    void drawables_shorthandMesh() {
        Drawables d = Drawables.singleMesh("models/cube.obj");
        assertEquals(1, d.parts().size());
        assertEquals("default", d.parts().get(0).id());
    }
}
