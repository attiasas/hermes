package dev.hermes.core.render.resource;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.render.PipelineDocument;
import dev.hermes.core.render.ShaderCompileException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Compiles and caches GLSL shaders declared in a {@link PipelineDocument}. */
public final class ShaderRegistry implements Disposable {

  public static final String DEFAULT_UNLIT = "default/unlit";

  private final Map<String, ShaderProgram> programs = new HashMap<>();
  private final Set<String> builtinIds = new HashSet<>();

  public ShaderRegistry(Map<String, PipelineDocument.ShaderDef> shaders) {
    if (shaders != null) {
      for (Map.Entry<String, PipelineDocument.ShaderDef> entry : shaders.entrySet()) {
        register(entry.getKey(), entry.getValue());
      }
    }
  }

  public boolean isRegistered(String shaderId) {
    return usesBuiltin(shaderId) || programs.containsKey(shaderId);
  }

  public boolean usesBuiltin(String shaderId) {
    return builtinIds.contains(shaderId);
  }

  public ShaderProgram requireProgram(String shaderId) {
    ShaderProgram program = programs.get(shaderId);
    if (program == null) {
      throw new ShaderCompileException("shader not registered: " + shaderId);
    }
    return program;
  }

  /**
   * Returns a libGDX {@link Shader} for the given id, or {@code null} when the registry should use
   * libGDX's built-in default shader provider (missing assets for {@value #DEFAULT_UNLIT}).
   */
  public Shader resolveG3dShader(String shaderId, Renderable renderable, Environment environment) {
    if (usesBuiltin(shaderId)) {
      return null;
    }
    ShaderProgram program = requireProgram(shaderId);
    Renderable scoped = renderable;
    scoped.environment = environment;
    return new DefaultShader(scoped, new DefaultShader.Config(), program);
  }

  private void register(String id, PipelineDocument.ShaderDef def) {
    FileHandle vertexFile = HermesAssetPaths.internal(def.vertex());
    FileHandle fragmentFile = HermesAssetPaths.internal(def.fragment());
    boolean vertexMissing = !vertexFile.exists();
    boolean fragmentMissing = !fragmentFile.exists();

    if (vertexMissing || fragmentMissing) {
      if (DEFAULT_UNLIT.equals(id)) {
        builtinIds.add(id);
        return;
      }
      throw new ShaderCompileException(
          "shader asset not found for '"
              + id
              + "' (vertex: "
              + def.vertex()
              + ", fragment: "
              + def.fragment()
              + ")");
    }

    String vertexSource = vertexFile.readString(StandardCharsets.UTF_8.name());
    String fragmentSource = fragmentFile.readString(StandardCharsets.UTF_8.name());
    ShaderProgram program = new ShaderProgram(vertexSource, fragmentSource);
    if (!program.isCompiled()) {
      throw new ShaderCompileException(
          "failed to compile shader '"
              + id
              + "' (vertex: "
              + def.vertex()
              + ", fragment: "
              + def.fragment()
              + "): "
              + program.getLog());
    }
    programs.put(id, program);
  }

  @Override
  public void dispose() {
    for (ShaderProgram program : programs.values()) {
      program.dispose();
    }
    programs.clear();
    builtinIds.clear();
  }
}
