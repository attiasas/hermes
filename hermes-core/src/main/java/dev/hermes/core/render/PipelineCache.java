package dev.hermes.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.HashMap;
import java.util.Map;

/** Caches compiled {@link RenderGraph} instances keyed by pipeline asset path. */
public final class PipelineCache {

  private final SpriteBatch batch;
  private final RenderGraphBuilder builder = new RenderGraphBuilder();
  private final Map<String, RenderGraph> graphs = new HashMap<>();

  public PipelineCache(SpriteBatch batch) {
    this.batch = batch;
  }

  /** Uses stub passes (no libGDX GL); for unit tests only. */
  PipelineCache() {
    this.batch = null;
  }

  public RenderGraph get(String assetPath) {
    return graphs.computeIfAbsent(
        assetPath,
        path -> {
          PipelineDocument document = PipelineLoader.load(path);
          return batch == null
              ? builder.buildWithStubs(document)
              : builder.build(document, batch);
        });
  }

  public void resize(int width, int height) {
    for (RenderGraph graph : graphs.values()) {
      graph.resize(width, height);
    }
  }

  public void dispose() {
    for (RenderGraph graph : graphs.values()) {
      graph.dispose();
    }
    graphs.clear();
  }
}
