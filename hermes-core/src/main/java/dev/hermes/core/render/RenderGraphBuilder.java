package dev.hermes.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.core.render.pass.SpritesPass;
import dev.hermes.core.render.pass.UiPass;
import dev.hermes.core.render.pass.World3dPass;
import dev.hermes.core.render.resource.ModelCache;
import dev.hermes.core.render.resource.ShaderRegistry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Builds a {@link RenderGraph} from a parsed {@link PipelineDocument}. */
public final class RenderGraphBuilder {

  private final ModelCache sharedModelCache = new ModelCache();

  public RenderGraph build(PipelineDocument document, SpriteBatch batch) {
    ShaderRegistry shaderRegistry = new ShaderRegistry(document.shaders());
    List<RenderGraphPass> passes = new ArrayList<>();
    for (PipelineDocument.PassDef passDef : document.passes()) {
      Set<RenderLayer.Layer> layers = parseLayers(passDef.layers());
      passes.add(createPass(passDef, batch, layers, shaderRegistry));
    }
    return new RenderGraph(document.clearColor(), passes, sharedModelCache, shaderRegistry);
  }

  /** Builds a graph with no-op passes for unit tests (same order and validation as {@link #build}). */
  RenderGraph buildWithStubs(PipelineDocument document) {
    List<RenderGraphPass> passes = new ArrayList<>();
    for (PipelineDocument.PassDef passDef : document.passes()) {
      parseLayers(passDef.layers());
      passes.add(new StubRenderGraphPass(passDef.id()));
    }
    return new RenderGraph(document.clearColor(), passes, null);
  }

  private RenderGraphPass createPass(
      PipelineDocument.PassDef passDef,
      SpriteBatch batch,
      Set<RenderLayer.Layer> layers,
      ShaderRegistry shaderRegistry) {
    switch (passDef.type()) {
      case WORLD3D:
        return new World3dGraphPass(
            passDef.id(),
            new World3dPass(sharedModelCache, shaderRegistry, false),
            layers,
            passDef.depthTest());
      case SPRITES:
        return new SpritesGraphPass(
            passDef.id(), new SpritesPass(batch, shaderRegistry), layers, passDef.depthTest());
      case UI:
        return new UiGraphPass(passDef.id(), new UiPass(batch), layers, passDef.depthTest());
      default:
        throw new PipelineParseException("unsupported pass type: " + passDef.type());
    }
  }

  private static Set<RenderLayer.Layer> parseLayers(List<String> layerNames) {
    EnumSet<RenderLayer.Layer> layers = EnumSet.noneOf(RenderLayer.Layer.class);
    for (String name : layerNames) {
      layers.add(parseLayer(name));
    }
    if (layers.isEmpty()) {
      layers.add(RenderLayer.Layer.WORLD);
    }
    return layers;
  }

  private static RenderLayer.Layer parseLayer(String name) {
    if (name == null || name.isBlank()) {
      return RenderLayer.Layer.WORLD;
    }
    try {
      return RenderLayer.Layer.valueOf(name.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new PipelineParseException("unknown render layer: " + name);
    }
  }
}
