package dev.hermes.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches compiled {@link RenderGraph} instances keyed by pipeline asset path.
 */
public final class PipelineCache {

    private final SpriteBatch batch;
    private final RenderGraphBuilder builder;
    private final ViewportServiceImpl viewport;
    private final Map<String, RenderGraph> graphs = new HashMap<>();
    private final Map<String, RuntimeException> failures = new HashMap<>();

    public PipelineCache(SpriteBatch batch, RenderPassRegistry passRegistry, ViewportServiceImpl viewport) {
        this.batch = batch;
        this.viewport = viewport == null ? new ViewportServiceImpl() : viewport;
        this.builder = new RenderGraphBuilder(passRegistry, this.viewport);
    }

    /**
     * Uses stub passes (no libGDX GL); for unit tests only.
     */
    PipelineCache(RenderPassRegistry passRegistry) {
        this(null, passRegistry, new ViewportServiceImpl());
    }

    PipelineCache(RenderPassRegistry passRegistry, ViewportServiceImpl viewport) {
        this(null, passRegistry, viewport);
    }

    PipelineCache() {
        this(new RenderPassRegistry());
    }

    public RenderGraph get(String assetPath) {
        RuntimeException cached = failures.get(assetPath);
        if (cached != null) {
            throw cached;
        }
        try {
            return graphs.computeIfAbsent(
                    assetPath,
                    path -> {
                        PipelineDocument document = PipelineLoader.load(path);
                        RenderGraph graph =
                                batch == null
                                        ? builder.buildWithStubs(document)
                                        : builder.build(document, batch);
                        graph.resize(
                                Math.max(1, viewport.windowWidth()),
                                Math.max(1, viewport.windowHeight()));
                        return graph;
                    });
        } catch (RuntimeException error) {
            failures.put(assetPath, error);
            throw error;
        }
    }

    public void resize(int width, int height) {
        viewport.onWindowResize(width, height);
        for (RenderGraph graph : graphs.values()) {
            graph.resize(width, height);
        }
    }

    public void dispose() {
        for (RenderGraph graph : graphs.values()) {
            graph.dispose();
        }
        graphs.clear();
        failures.clear();
    }

    /** Builds and caches a graph from an in-memory document (tests only). */
    RenderGraph getForTest(PipelineDocument document) {
        String key = "test:" + document.hashCode();
        return graphs.computeIfAbsent(
                key,
                ignored -> {
                    RenderGraph graph =
                            batch == null
                                    ? builder.buildWithStubs(document)
                                    : builder.build(document, batch);
                    graph.resize(
                            Math.max(1, viewport.windowWidth()),
                            Math.max(1, viewport.windowHeight()));
                    return graph;
                });
    }
}
