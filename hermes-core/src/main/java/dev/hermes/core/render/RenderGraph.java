package dev.hermes.core.render;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.viewport.BackbufferSize;
import dev.hermes.core.render.resource.ModelCache;
import dev.hermes.core.render.resource.ShaderRegistry;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.util.List;

/**
 * Compiled render pipeline: ordered passes and frame clear color.
 */
public final class RenderGraph {

    private final float[] clearColor;
    private final List<RenderGraphPass> passes;
    private final ModelCache sharedModelCache;
    private final ShaderRegistry shaderRegistry;
    private final FramebufferPool framebufferPool;
    private final ViewportServiceImpl viewport;
    private int backbufferWidth = -1;
    private int backbufferHeight = -1;

    RenderGraph(float[] clearColor, List<RenderGraphPass> passes, ModelCache sharedModelCache) {
        this(clearColor, passes, sharedModelCache, null, null, new ViewportServiceImpl());
    }

    RenderGraph(
            float[] clearColor,
            List<RenderGraphPass> passes,
            ModelCache sharedModelCache,
            ShaderRegistry shaderRegistry) {
        this(clearColor, passes, sharedModelCache, shaderRegistry, null, new ViewportServiceImpl());
    }

    RenderGraph(
            float[] clearColor,
            List<RenderGraphPass> passes,
            ModelCache sharedModelCache,
            ShaderRegistry shaderRegistry,
            FramebufferPool framebufferPool,
            ViewportServiceImpl viewport) {
        this.clearColor = clearColor.clone();
        this.passes = List.copyOf(passes);
        this.sharedModelCache = sharedModelCache;
        this.shaderRegistry = shaderRegistry;
        this.framebufferPool = framebufferPool;
        this.viewport = viewport;
    }

    public float[] clearColor() {
        return clearColor.clone();
    }

    public int passCount() {
        return passes.size();
    }

    public String passId(int index) {
        return passes.get(index).id();
    }

    /**
     * Pass render target id ({@code "screen"} or a framebuffer id); for tests.
     */
    String passTarget(int index) {
        RenderGraphPass pass = passes.get(index);
        if (pass instanceof TargetBindingGraphPass) {
            return ((TargetBindingGraphPass) pass).target();
        }
        return "screen";
    }

    FramebufferPool framebufferPool() {
        return framebufferPool;
    }

    public void resize(int width, int height) {
        backbufferWidth = Math.max(1, width);
        backbufferHeight = Math.max(1, height);
        if (viewport != null) {
            viewport.onWindowResize(backbufferWidth, backbufferHeight);
        }
        if (framebufferPool != null) {
            framebufferPool.resize(backbufferWidth, backbufferHeight);
        }
        for (RenderGraphPass pass : passes) {
            pass.resize(backbufferWidth, backbufferHeight);
        }
    }

    public void render(EntityStore entities) {
        syncBackbufferSize();
        for (RenderGraphPass pass : passes) {
            pass.render(entities, null, null);
        }
    }

    private void syncBackbufferSize() {
        int width = BackbufferSize.width();
        int height = BackbufferSize.height();
        if (width != backbufferWidth || height != backbufferHeight) {
            resize(width, height);
        }
    }

    public void dispose() {
        for (RenderGraphPass pass : passes) {
            pass.dispose();
        }
        if (sharedModelCache != null) {
            sharedModelCache.dispose();
        }
        if (shaderRegistry != null) {
            shaderRegistry.dispose();
        }
        if (framebufferPool != null) {
            framebufferPool.dispose();
        }
    }
}
