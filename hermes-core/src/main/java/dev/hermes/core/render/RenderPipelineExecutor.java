package dev.hermes.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.World;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.core.scene.SceneInstance;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.util.Objects;
import java.util.Optional;

/**
 * Loads JSON render pipelines and executes the resolved graph for each visible scene world.
 */
public final class RenderPipelineExecutor {

    private final String projectDefaultPipelinePath;
    private final PipelineCache cache;
    private final ViewportServiceImpl viewport;

    public RenderPipelineExecutor(
            SpriteBatch batch,
            String projectDefaultPipelinePath,
            RenderPassRegistry passRegistry,
            ViewportServiceImpl viewport) {
        this.projectDefaultPipelinePath =
                Objects.requireNonNull(projectDefaultPipelinePath, "projectDefaultPipelinePath");
        if (this.projectDefaultPipelinePath.isBlank()) {
            throw new IllegalArgumentException("project default render pipeline path is required");
        }
        this.viewport = viewport == null ? new ViewportServiceImpl() : viewport;
        this.cache = new PipelineCache(batch, passRegistry, this.viewport);
    }

    public ViewportServiceImpl viewport() {
        return viewport;
    }

    public void resize(int width, int height) {
        cache.resize(width, height);
    }

    public void execute(Iterable<? extends SceneHandle> scenes) {
        boolean cleared = false;
        for (SceneHandle scene : scenes) {
            RenderGraph graph = cache.get(resolvePipelinePath(scene));
            if (!cleared) {
                float[] clear = graph.clearColor();
                Gdx.gl.glClearColor(clear[0], clear[1], clear[2], clear[3]);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
                cleared = true;
            }
            World world = scene.world();
            if (world != null) {
                graph.render(world);
            }
        }
    }

    public static String resolvePipelinePath(SceneHandle scene, String projectDefault) {
        Optional<String> jsonOverride = scene.renderPipelineOverride();
        if (jsonOverride.isPresent()) {
            return jsonOverride.get();
        }
        if (scene instanceof SceneInstance) {
            Optional<String> definitionPath = ((SceneInstance) scene).definition().renderPipeline();
            if (definitionPath.isPresent()) {
                return definitionPath.get();
            }
        }
        return projectDefault;
    }

    private String resolvePipelinePath(SceneHandle scene) {
        return resolvePipelinePath(scene, projectDefaultPipelinePath);
    }

    public void dispose() {
        cache.dispose();
    }
}
