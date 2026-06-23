package dev.hermes.core.ecs;

import dev.hermes.api.ecs.ComponentContext;
import dev.hermes.api.ecs.DrawableKind;
import dev.hermes.api.ecs.Drawables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class DrawablesDeserializerTest {

    @Test
    void shorthandSprite() {
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        Drawables d =
                (Drawables)
                        registry.deserialize(
                                "Drawables",
                                JsonComponentData.parse("{\"sprite\":\"logo.png\"}"),
                                ComponentContext.EMPTY);
        assertEquals(1, d.parts().size());
        assertEquals(DrawableKind.SPRITE, d.parts().get(0).kind());
        assertEquals("logo.png", d.parts().get(0).texture());
    }

    @Test
    void multiPartMesh() {
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        String json = "{\"parts\":[{\"id\":\"a\",\"kind\":\"mesh\",\"model\":\"models/cube.obj\"}]}";
        Drawables d =
                (Drawables)
                        registry.deserialize(
                                "Drawables", JsonComponentData.parse(json), ComponentContext.EMPTY);
        assertEquals("a", d.parts().get(0).id());
    }
}
