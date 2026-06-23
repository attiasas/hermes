package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

final class MeshMaterialParserTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void meshDeserializesModelAndTexture() {
        Drawables drawables = deserializeDrawables("{\"mesh\":\"models/cube.obj\",\"texture\":\"tex.png\"}");
        assertEquals("models/cube.obj", drawables.parts().get(0).model());
        assertEquals("tex.png", drawables.parts().get(0).texture());
    }

    @Test
    void materialDeserializesShaderAndUniforms() {
        Material m =
                deserializeMaterial("{\"shader\":\"default/unlit\",\"uniforms\":{\"u_tint\":[1,0,0,1]}}");
        assertEquals("default/unlit", m.shader());
        assertEquals(1f, m.uniform("u_tint").getAsFloatArray()[0], 0.001f);
    }

    private Drawables deserializeDrawables(String json) {
        return (Drawables)
                registry.deserialize(
                        "test.json",
                        "entity",
                        BuiltinComponents.DRAWABLES,
                        new JsonComponentData(new JsonReader().parse(json)),
                        emptyContext());
    }

    private Material deserializeMaterial(String json) {
        return (Material)
                registry.deserialize(
                        "test.json",
                        "entity",
                        BuiltinComponents.MATERIAL,
                        new JsonComponentData(new JsonReader().parse(json)),
                        emptyContext());
    }

    private static ComponentContextImpl emptyContext() {
        return new ComponentContextImpl(new EntityId(1), EntityKind.UNSET, "entity", Map.of());
    }
}
