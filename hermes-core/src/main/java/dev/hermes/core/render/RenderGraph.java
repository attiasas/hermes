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

  RenderGraph(float[] clearColor, List<RenderGraphPass> passes, ModelCache sharedModelCache) {
    this(clearColor, passes, sharedModelCache, null);
  }

  RenderGraph(
      float[] clearColor,
      List<RenderGraphPass> passes,
      ModelCache sharedModelCache,
      ShaderRegistry shaderRegistry) {
    this.clearColor = clearColor.clone();
    this.passes = List.copyOf(passes);
    this.sharedModelCache = sharedModelCache;
    this.shaderRegistry = shaderRegistry;
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

  public void resize(int width, int height) {
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
  }
}
