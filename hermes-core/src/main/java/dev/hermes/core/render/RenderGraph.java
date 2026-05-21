package dev.hermes.core.render;

import dev.hermes.api.ecs.World;
import dev.hermes.core.render.resource.ModelCache;
import dev.hermes.core.render.resource.ShaderRegistry;
import java.util.List;

/** Compiled render pipeline: ordered passes and frame clear color. */
public final class RenderGraph {

  private final float[] clearColor;
  private final List<RenderGraphPass> passes;
  private final ModelCache sharedModelCache;
  private final ShaderRegistry shaderRegistry;
  private final FramebufferPool framebufferPool;

  RenderGraph(float[] clearColor, List<RenderGraphPass> passes, ModelCache sharedModelCache) {
    this(clearColor, passes, sharedModelCache, null, null);
  }

  RenderGraph(
      float[] clearColor,
      List<RenderGraphPass> passes,
      ModelCache sharedModelCache,
      ShaderRegistry shaderRegistry) {
    this(clearColor, passes, sharedModelCache, shaderRegistry, null);
  }

  RenderGraph(
      float[] clearColor,
      List<RenderGraphPass> passes,
      ModelCache sharedModelCache,
      ShaderRegistry shaderRegistry,
      FramebufferPool framebufferPool) {
    this.clearColor = clearColor.clone();
    this.passes = List.copyOf(passes);
    this.sharedModelCache = sharedModelCache;
    this.shaderRegistry = shaderRegistry;
    this.framebufferPool = framebufferPool;
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

  /** Pass render target id ({@code "screen"} or a framebuffer id); for tests. */
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
    if (framebufferPool != null) {
      framebufferPool.resize(width, height);
    }
    for (RenderGraphPass pass : passes) {
      pass.resize(width, height);
    }
  }

  public void render(World world) {
    for (RenderGraphPass pass : passes) {
      pass.render(world);
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
