package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/** Default centered progress bar and percent text for async resource loads. */
final class BuiltinLoadingScreen implements Disposable {

    private static final float BAR_WIDTH_FRACTION = 0.55f;
    private static final float BAR_HEIGHT = 24f;
    private static final float OVERLAY_ALPHA = 0.82f;

    private TextureRegion whitePixel;
    private BitmapFont font;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    void render(SpriteBatch batch, int width, int height, float progress, String label) {
        ensureGraphics();
        float clamped = Math.min(1f, Math.max(0f, progress));

        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        batch.begin();

        batch.setColor(0.05f, 0.05f, 0.07f, OVERLAY_ALPHA);
        batch.draw(whitePixel, 0f, 0f, width, height);

        float barWidth = width * BAR_WIDTH_FRACTION;
        float barX = (width - barWidth) * 0.5f;
        float barY = height * 0.5f - BAR_HEIGHT * 0.5f;

        if (label != null && !label.isBlank() && font != null) {
            font.setColor(0.92f, 0.92f, 0.94f, 1f);
            glyphLayout.setText(font, label);
            float labelX = (width - glyphLayout.width) * 0.5f;
            float labelY = barY + BAR_HEIGHT + glyphLayout.height + 12f;
            font.draw(batch, glyphLayout, labelX, labelY);
        }

        batch.setColor(0.15f, 0.15f, 0.15f, 0.9f);
        batch.draw(whitePixel, barX, barY, barWidth, BAR_HEIGHT);
        batch.setColor(0.2f, 0.75f, 0.35f, 1f);
        batch.draw(whitePixel, barX, barY, barWidth * clamped, BAR_HEIGHT);

        String percentText = Math.round(clamped * 100f) + "%";
        if (font != null) {
            font.setColor(1f, 1f, 1f, 1f);
            glyphLayout.setText(font, percentText);
            float percentX = (width - glyphLayout.width) * 0.5f;
            float percentY = barY - 10f;
            font.draw(batch, glyphLayout, percentX, percentY);
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        if (whitePixel != null) {
            whitePixel.getTexture().dispose();
            whitePixel = null;
        }
        if (font != null) {
            font.dispose();
            font = null;
        }
    }

    private void ensureGraphics() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            whitePixel = new TextureRegion(texture);
        }
        if (font == null) {
            try {
                font = new BitmapFont();
            } catch (RuntimeException ignored) {
                // Classpath/headless tests: progress bar only, no labels
            }
        }
    }
}
