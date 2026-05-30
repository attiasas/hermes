package dev.hermes.core.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.render.pass.UiRenderPass;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

final class UiGraphPass implements RenderGraphPass {

    private final String id;
    private final UiRenderPass delegate;
    private final boolean depthTest;

    UiGraphPass(String id, UiRenderPass delegate, boolean depthTest) {
        this.id = id;
        this.delegate = delegate;
        this.depthTest = depthTest;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render(EntityStore entities, RenderSurface surface, BoundCamera bound) {
        if (!depthTest) {
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        }
        delegate.render(entities, bound);
        if (!depthTest) {
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        }
    }

    @Override
    public void dispose() {
    }
}
