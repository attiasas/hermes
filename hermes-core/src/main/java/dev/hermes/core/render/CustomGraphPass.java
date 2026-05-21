package dev.hermes.core.render;

import dev.hermes.api.ecs.World;
import dev.hermes.api.render.RenderPass;

final class CustomGraphPass implements RenderGraphPass {

  private final String id;
  private final RenderPass delegate;

  CustomGraphPass(String id, RenderPass delegate) {
    this.id = id;
    this.delegate = delegate;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public void resize(int width, int height) {
    delegate.resize(width, height);
  }

  @Override
  public void render(World world) {
    delegate.render(world);
  }

  @Override
  public void dispose() {
    delegate.dispose();
  }
}
