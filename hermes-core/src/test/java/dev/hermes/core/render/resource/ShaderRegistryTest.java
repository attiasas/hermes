package dev.hermes.core.render.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.hermes.core.TestGdx;
import dev.hermes.core.render.PipelineDocument;
import dev.hermes.core.render.ShaderCompileException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ShaderRegistryTest {

  @BeforeEach
  void setUp() {
    TestGdx.initHeadlessGl();
  }

  @Test
  void defaultUnlitFromClasspath_isG3dOnly_notSpriteCapable() {
    PipelineDocument.ShaderDef def =
        new PipelineDocument.ShaderDef("shaders/default.vert", "shaders/default.frag");
    ShaderRegistry registry = new ShaderRegistry(Map.of(ShaderRegistry.DEFAULT_UNLIT, def));

    assertFalse(registry.supportsSpriteBatch(ShaderRegistry.DEFAULT_UNLIT));
    assertTrue(registry.isRegistered(ShaderRegistry.DEFAULT_UNLIT));
    registry.dispose();
  }

  @Test
  void loadsAndCompilesShaderFromClasspathAssets() {
    PipelineDocument.ShaderDef def =
        new PipelineDocument.ShaderDef("shaders/default.vert", "shaders/default.frag");
    ShaderRegistry registry = new ShaderRegistry(Map.of(ShaderRegistry.DEFAULT_UNLIT, def));

    assertFalse(registry.usesBuiltin(ShaderRegistry.DEFAULT_UNLIT));
    ShaderProgram program = registry.requireProgram(ShaderRegistry.DEFAULT_UNLIT);
    assertTrue(program.isCompiled());
    registry.dispose();
  }

  @Test
  void missingDefaultUnlitAssets_fallsBackToBuiltin() {
    PipelineDocument.ShaderDef def =
        new PipelineDocument.ShaderDef("shaders/missing.vert", "shaders/missing.frag");
    ShaderRegistry registry = new ShaderRegistry(Map.of(ShaderRegistry.DEFAULT_UNLIT, def));

    assertTrue(registry.usesBuiltin(ShaderRegistry.DEFAULT_UNLIT));
    assertThrows(ShaderCompileException.class, () -> registry.requireProgram(ShaderRegistry.DEFAULT_UNLIT));
    registry.dispose();
  }

  @Test
  void compileFailure_includesAssetPathsInMessage() {
    PipelineDocument.ShaderDef def =
        new PipelineDocument.ShaderDef("shaders/broken.vert", "shaders/broken.frag");
    ShaderCompileException error =
        assertThrows(ShaderCompileException.class, () -> new ShaderRegistry(Map.of("broken", def)));

    assertTrue(error.getMessage().contains("broken.vert"));
    assertTrue(error.getMessage().contains("broken.frag"));
  }

  @Test
  void missingNonDefaultShader_reportsAssetPaths() {
    PipelineDocument.ShaderDef def =
        new PipelineDocument.ShaderDef("shaders/nope.vert", "shaders/nope.frag");
    ShaderCompileException error =
        assertThrows(
            ShaderCompileException.class,
            () -> new ShaderRegistry(Map.of("water", def)));

    assertEquals(true, error.getMessage().contains("water"));
    assertTrue(error.getMessage().contains("nope.vert"));
    assertTrue(error.getMessage().contains("nope.frag"));
  }
}
