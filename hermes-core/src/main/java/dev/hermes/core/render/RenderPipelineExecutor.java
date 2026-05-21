package dev.hermes.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneHandle;

/** Loads a JSON render pipeline and executes it for each visible scene world. */
public final class RenderPipelineExecutor {

  private final RenderGraph graph;

  public RenderPipelineExecutor(SpriteBatch batch, String pipelineAssetPath) {
    PipelineDocument document = PipelineLoader.load(pipelineAssetPath);
    this.graph = new RenderGraphBuilder().build(document, batch);
  }

  public void resize(int width, int height) {
    graph.resize(width, height);
  }

  public void execute(Iterable<? extends SceneHandle> scenes) {
    float[] clear = graph.clearColor();
    Gdx.gl.glClearColor(clear[0], clear[1], clear[2], clear[3]);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    for (SceneHandle scene : scenes) {
      World world = scene.world();
      if (world != null) {
        graph.render(world);
      }
    }
  }

  public void dispose() {
    graph.dispose();
  }
}
