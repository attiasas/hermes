package dev.hermes.core.render.pass;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.core.ui.UiServiceImpl;
import dev.hermes.core.viewport.BackbufferSize;
import dev.hermes.core.viewport.BoundCamera;
import dev.hermes.core.viewport.RenderSurface;

/** Renders scene UI widget trees via {@link UiServiceImpl}. */
public final class UiRenderPass {

    private final UiServiceImpl ui;
    private final SpriteBatch batch;

    public UiRenderPass() {
        this(null, null);
    }

    public UiRenderPass(UiServiceImpl ui, SpriteBatch batch) {
        this.ui = ui;
        this.batch = batch;
    }

    public void render(EntityStore entities, RenderSurface surface, BoundCamera bound, String sceneId) {
        if (ui == null || batch == null || sceneId == null || sceneId.isBlank()) {
            return;
        }
        int width = surface != null ? surface.pixelWidth() : BackbufferSize.width();
        int height = surface != null ? surface.pixelHeight() : BackbufferSize.height();
        ui.layoutAndRender(sceneId, batch, width, height);
    }
}
