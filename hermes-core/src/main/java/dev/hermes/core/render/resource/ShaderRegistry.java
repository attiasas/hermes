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

/**
 * Compiles and caches GLSL shaders declared in a {@link PipelineDocument}.
 */
public final class ShaderRegistry implements Disposable {

    public static final String DEFAULT_UNLIT = "default/unlit";

    private final Map<String, RegisteredShader> shaders = new HashMap<>();
    private final Set<String> builtinIds = new HashSet<>();

    public ShaderRegistry(Map<String, PipelineDocument.ShaderDef> shaders) {
        if (shaders != null) {
            for (Map.Entry<String, PipelineDocument.ShaderDef> entry : shaders.entrySet()) {
                register(entry.getKey(), entry.getValue());
            }
        }
    }

    public boolean isRegistered(String shaderId) {
        return usesBuiltin(shaderId) || shaders.containsKey(shaderId);
    }

    public boolean usesBuiltin(String shaderId) {
        return builtinIds.contains(shaderId);
    }

    /**
     * True when the shader can be bound on libGDX {@link com.badlogic.gdx.graphics.g2d.SpriteBatch}.
     */
    public boolean supportsSpriteBatch(String shaderId) {
        if (usesBuiltin(shaderId)) {
            return true;
        }
        RegisteredShader shader = shaders.get(shaderId);
        return shader != null && shader.supportsSpriteBatch();
    }

    public ShaderProgram requireProgram(String shaderId) {
        RegisteredShader shader = shaders.get(shaderId);
        if (shader == null) {
            throw new ShaderCompileException("shader not registered: " + shaderId);
        }
        return shader.spriteProgram();
    }

    /**
     * Returns a libGDX {@link Shader} for the given id, or {@code null} when the registry should use
     * libGDX's built-in default shader provider (missing assets for {@value #DEFAULT_UNLIT}).
     */
    public Shader resolveG3dShader(String shaderId, Renderable renderable, Environment environment) {
        if (usesBuiltin(shaderId)) {
            return null;
        }
        RegisteredShader sources = shaders.get(shaderId);
        if (sources == null) {
            throw new ShaderCompileException("shader not registered: " + shaderId);
        }
        Renderable scoped = renderable;
        scoped.environment = environment;
        DefaultShader.Config config = new DefaultShader.Config();
        config.numDirectionalLights = 1;
        config.numPointLights = 0;
        config.numSpotLights = 0;
        DefaultShader shader =
                new DefaultShader(
                        scoped, config, "", sources.vertexSource(), sources.fragmentSource());
        shader.init();
        return shader;
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
        program.dispose();
        shaders.put(
                id, new RegisteredShader(vertexSource, fragmentSource, vertexSource.contains("u_projTrans")));
    }

    @Override
    public void dispose() {
        for (RegisteredShader shader : shaders.values()) {
            shader.dispose();
        }
        shaders.clear();
        builtinIds.clear();
    }

    private static final class RegisteredShader {
        private final String vertexSource;
        private final String fragmentSource;
        private final boolean supportsSpriteBatch;
        private ShaderProgram spriteProgram;

        private RegisteredShader(String vertexSource, String fragmentSource, boolean supportsSpriteBatch) {
            this.vertexSource = vertexSource;
            this.fragmentSource = fragmentSource;
            this.supportsSpriteBatch = supportsSpriteBatch;
        }

        private boolean supportsSpriteBatch() {
            return supportsSpriteBatch;
        }

        private String vertexSource() {
            return vertexSource;
        }

        private String fragmentSource() {
            return fragmentSource;
        }

        private ShaderProgram spriteProgram() {
            if (spriteProgram == null) {
                spriteProgram = new ShaderProgram(vertexSource, fragmentSource);
                if (!spriteProgram.isCompiled()) {
                    throw new ShaderCompileException("failed to compile sprite shader: " + spriteProgram.getLog());
                }
            }
            return spriteProgram;
        }

        private void dispose() {
            if (spriteProgram != null) {
                spriteProgram.dispose();
                spriteProgram = null;
            }
        }
    }
}
