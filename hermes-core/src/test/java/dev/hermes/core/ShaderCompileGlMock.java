package dev.hermes.core;

import com.badlogic.gdx.graphics.GL20;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal {@link GL20} proxy so {@link com.badlogic.gdx.graphics.glutils.ShaderProgram} can compile in unit tests.
 */
final class ShaderCompileGlMock {

    private ShaderCompileGlMock() {
    }

    static GL20 create() {
        return (GL20)
                Proxy.newProxyInstance(
                        GL20.class.getClassLoader(), new Class<?>[]{GL20.class}, new Handler());
    }

    private static final class Handler implements InvocationHandler {
        private final Map<Integer, String> shaderSources = new HashMap<>();
        private int nextShaderId = 1;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            switch (name) {
                case "glCreateShader":
                    return nextShaderId++;
                case "glShaderSource":
                    shaderSources.put((Integer) args[0], (String) args[1]);
                    return null;
                case "glCompileShader":
                    return null;
                case "glGetShaderiv":
                    writeCompileStatus((Integer) args[0], (IntBuffer) args[2]);
                    return null;
                case "glGetShaderInfoLog":
                    return "compile error";
                case "glDeleteShader":
                    shaderSources.remove(args[0]);
                    return null;
                case "glCreateProgram":
                    return 100;
                case "glAttachShader":
                case "glLinkProgram":
                case "glUseProgram":
                case "glDetachShader":
                case "glDeleteProgram":
                    return null;
                case "glGetProgramiv":
                    writeProgramInt((Integer) args[1], (IntBuffer) args[2]);
                    return null;
                case "glGetActiveAttrib":
                case "glGetActiveUniform":
                    return "a_position";
                case "glGetAttribLocation":
                case "glGetUniformLocation":
                    return 0;
                case "glGetProgramInfoLog":
                    return "";
                default:
                    break;
            }
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == float.class) {
                return 0f;
            }
            if (returnType == void.class) {
                return null;
            }
            return null;
        }

        private void writeCompileStatus(int shader, IntBuffer buffer) {
            String source = shaderSources.get(shader);
            int status = source != null && source.contains("will not compile") ? 0 : 1;
            buffer.clear();
            buffer.put(status);
            buffer.flip();
        }

        private static void writeProgramInt(int pname, IntBuffer buffer) {
            int value;
            if (pname == GL20.GL_LINK_STATUS) {
                value = 1;
            } else if (pname == GL20.GL_ACTIVE_ATTRIBUTES || pname == GL20.GL_ACTIVE_UNIFORMS) {
                value = 0;
            } else {
                value = 0;
            }
            buffer.clear();
            buffer.put(value);
            buffer.flip();
        }
    }
}
