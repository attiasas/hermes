package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.Mesh;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class MeshMaterialParserTest {

  private ComponentRegistryImpl registry;

  @BeforeEach
  void setUp() {
    registry = new ComponentRegistryImpl();
    BuiltinComponents.register(registry);
  }

  @Test
  void meshDeserializesModelAndTexture() {
    Mesh mesh = deserializeMesh("{\"model\":\"models/cube.obj\",\"texture\":\"tex.png\"}");
    assertEquals("models/cube.obj", mesh.model());
    assertEquals("tex.png", mesh.texture());
  }

  @Test
  void materialDeserializesShaderAndUniforms() {
    Material m =
        deserializeMaterial("{\"shader\":\"default/unlit\",\"uniforms\":{\"u_tint\":[1,0,0,1]}}");
    assertEquals("default/unlit", m.shader());
    assertEquals(1f, m.uniform("u_tint").getAsFloatArray()[0], 0.001f);
  }

  private Mesh deserializeMesh(String json) {
    return (Mesh)
        registry.deserialize(
            "test.json",
            "entity",
            BuiltinComponents.MESH,
            new JsonComponentData(new JsonReader().parse(json)));
  }

  private Material deserializeMaterial(String json) {
    return (Material)
        registry.deserialize(
            "test.json",
            "entity",
            BuiltinComponents.MATERIAL,
            new JsonComponentData(new JsonReader().parse(json)));
  }
}
