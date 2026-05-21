package dev.hermes.core.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.World;
import java.util.function.Supplier;

/** On-screen FPS and entity count when debug mode is active. */
public final class DebugOverlay {

  private final Supplier<Boolean> debugEnabled;
  private BitmapFont font;
  private SpriteBatch batch;

  public DebugOverlay(Supplier<Boolean> debugEnabled) {
    this.debugEnabled = debugEnabled;
  }

  public boolean isActive() {
    return debugEnabled.get();
  }

  public void render(World world) {
    if (!isActive()) {
      return;
    }
    ensureAssets();
    batch.begin();
    int y = Gdx.graphics.getHeight() - 10;
    font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 8, y);
    font.draw(batch, "Entities: " + world.entityCount(), 8, y - 20);
    batch.end();
  }

  private void ensureAssets() {
    if (font == null) {
      font = new BitmapFont();
      batch = new SpriteBatch();
    }
  }

  public void dispose() {
    if (font != null) {
      font.dispose();
      font = null;
    }
    if (batch != null) {
      batch.dispose();
      batch = null;
    }
  }
}
