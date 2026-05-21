package dev.hermes.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.World;
import dev.hermes.api.scene.SceneHandle;
import java.util.Iterator;

/** Runs the builtin forward render pipeline for each visible scene world. */
public final class RenderPipelineExecutor {

  private final BuiltinForwardPipeline pipeline;

  public RenderPipelineExecutor(SpriteBatch batch) {
    this.pipeline = new BuiltinForwardPipeline(batch);
  }

  public void resize(int width, int height) {
    pipeline.resize(width, height);
  }

  public void execute(Iterable<? extends SceneHandle> scenes) {
    for (SceneHandle scene : scenes) {
      World world = scene.world();
      if (world != null) {
        pipeline.render(world);
      }
    }
  }

  public void dispose() {
    pipeline.dispose();
  }
}
