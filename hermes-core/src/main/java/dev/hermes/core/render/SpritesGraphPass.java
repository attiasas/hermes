package dev.hermes.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.ecs.World;
import dev.hermes.core.render.pass.SpritesPass;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

import java.util.Set;

final class SpritesGraphPass implements RenderGraphPass {

    private final String id;
    private final SpritesPass delegate;
    private final Set<RenderLayer.Layer> layers;
    private final boolean depthTest;

    SpritesGraphPass(String id, SpritesPass delegate, Set<RenderLayer.Layer> layers, boolean depthTest) {
        this.id = id;
        this.delegate = delegate;
        this.layers = Set.copyOf(layers);
        this.depthTest = depthTest;
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
    public void render(World world, RenderSurface surface, BoundCamera bound) {
        if (!depthTest) {
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        }
        delegate.render(world, layers, bound);
        if (!depthTest) {
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        }
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }
}
