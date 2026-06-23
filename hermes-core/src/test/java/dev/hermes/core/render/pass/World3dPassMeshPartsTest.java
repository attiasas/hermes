package dev.hermes.core.render.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.DrawableRig;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class World3dPassMeshPartsTest {

    private ResourceManagerImpl resources;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        resources = ResourceManagerImpl.createDefault();
    }

    @Test
    void shouldDrawMeshPart_countsTwoVisibleMeshParts() {
        Drawables drawables =
                new Drawables(
                        java.util.List.of(
                                DrawablePart.mesh("a", "models/cube.obj"),
                                DrawablePart.mesh("b", "models/cube.obj"),
                                DrawablePart.sprite("logo", "textures/test.png")));

        int drawableParts = 0;
        for (DrawablePart part : drawables.parts()) {
            if (World3dPass.shouldDrawMeshPart(part)) {
                drawableParts++;
            }
        }
        assertEquals(2, drawableParts);
    }

    @Test
    void shouldDrawMeshPart_skipsInvisibleAndBlankModel() {
        DrawablePart hidden = DrawablePart.mesh("hidden", "models/cube.obj");
        hidden.local().setVisible(false);
        DrawablePart noModel = DrawablePart.mesh("empty", "");

        assertFalse(World3dPass.shouldDrawMeshPart(hidden));
        assertFalse(World3dPass.shouldDrawMeshPart(noModel));
        assertFalse(World3dPass.shouldDrawMeshPart(DrawablePart.sprite("s", "tex.png")));
    }

    @Test
    void shouldDrawMeshPart_riggedPartWithModelDrawsStatic() {
        DrawablePart rigged = DrawablePart.mesh("skinned", "models/cube.obj");
        rigged.setRig(DrawableRig.GLTF);

        assertTrue(World3dPass.shouldDrawMeshPart(rigged));
    }

    @Test
    void shouldDrawMeshPart_riggedPartWithoutModelSkipped() {
        DrawablePart rigged = DrawablePart.mesh("skinned", "");
        rigged.setRig(DrawableRig.GLTF);

        assertFalse(World3dPass.shouldDrawMeshPart(rigged));
    }

    @Test
    void multiPartEntity_loadsModelForEachVisibleMeshPart() {
        Drawables drawables =
                new Drawables(
                        java.util.List.of(
                                DrawablePart.mesh("a", "models/cube.obj"),
                                DrawablePart.mesh("b", "models/cube.obj"),
                                hiddenPart(),
                                DrawablePart.sprite("logo", "textures/test-rgba.png")));

        int loads = 0;
        for (DrawablePart part : drawables.parts()) {
            if (!World3dPass.shouldDrawMeshPart(part)) {
                continue;
            }
            ResourceRef ref = ResourceRef.of(part.model());
            resources.loadSync(ref, ResourceKind.MODEL);
            assertTrue(resources.isLoaded(ref, ResourceKind.MODEL));
            loads++;
        }
        assertEquals(2, loads);
    }

    private static DrawablePart hiddenPart() {
        DrawablePart hidden = DrawablePart.mesh("hidden", "models/cube.obj");
        hidden.local().setVisible(false);
        return hidden;
    }
}
