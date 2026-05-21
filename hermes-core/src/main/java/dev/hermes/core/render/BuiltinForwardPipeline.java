package dev.hermes.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.World;
import dev.hermes.core.render.pass.SpritesPass;
import dev.hermes.core.render.pass.World3dPass;
import dev.hermes.core.render.resource.ModelCache;

/** Hardcoded forward pass order: world 3D meshes, then sprites. */
public final class BuiltinForwardPipeline {

  private final World3dPass world3dPass;
  private final SpritesPass spritesPass;

  public BuiltinForwardPipeline(SpriteBatch batch) {
    ModelCache modelCache = new ModelCache();
    this.world3dPass = new World3dPass(modelCache);
    this.spritesPass = new SpritesPass(batch);
  }

  public void resize(int width, int height) {
    world3dPass.resize(width, height);
    spritesPass.resize(width, height);
  }

  public void render(World world) {
    world3dPass.render(world);
    spritesPass.render(world);
  }

  public void dispose() {
    world3dPass.dispose();
    spritesPass.dispose();
  }
}
