package dev.hermes.core;

import com.badlogic.gdx.graphics.GL20;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link GL20} mock that records framebuffer bind calls for render pipeline tests.
 */
final class FramebufferGlMock {

    private FramebufferGlMock() {
    }

    static RecordingGl create() {
        return new RecordingGl();
    }

    static final class RecordingGl implements InvocationHandler {
        private final GL20 delegate = ShaderCompileGlMock.create();
        private final List<Integer> bindFramebufferCalls = new ArrayList<>();
        private final List<int[]> viewportCalls = new ArrayList<>();
        private int nextFramebufferId = 10;
        private int nextTextureId = 20;

        GL20 gl() {
            return (GL20) Proxy.newProxyInstance(GL20.class.getClassLoader(), new Class<?>[]{GL20.class}, this);
        }

        List<Integer> bindFramebufferCalls() {
            return List.copyOf(bindFramebufferCalls);
        }

        List<int[]> viewportCalls() {
            return List.copyOf(viewportCalls);
        }

        void clearViewportCalls() {
            viewportCalls.clear();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            switch (name) {
                case "glGenFramebuffer":
                    return nextFramebufferId++;
                case "glBindFramebuffer":
                    bindFramebufferCalls.add((Integer) args[1]);
                    return null;
                case "glCheckFramebufferStatus":
                    return GL20.GL_FRAMEBUFFER_COMPLETE;
                case "glGenTexture":
                    return nextTextureId++;
                case "glBindTexture":
                case "glTexImage2D":
                case "glTexParameteri":
                case "glFramebufferTexture2D":
                case "glClear":
                case "glClearColor":
                    return null;
                case "glViewport":
                    if (args != null && args.length >= 4) {
                        viewportCalls.add(
                                new int[]{
                                        ((Number) args[0]).intValue(),
                                        ((Number) args[1]).intValue(),
                                        ((Number) args[2]).intValue(),
                                        ((Number) args[3]).intValue()
                                });
                    }
                    return null;
                case "glGetIntegerv":
                    if (args != null && args.length >= 2 && args[1] instanceof IntBuffer) {
                        IntBuffer buffer = (IntBuffer) args[1];
                        buffer.clear();
                        buffer.put(1280);
                        buffer.flip();
                    }
                    return null;
                default:
                    break;
            }
            return method.invoke(delegate, args);
        }
    }
}
