package dev.hermes.core.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.api.ui.UiNode;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Walks a laid-out widget tree and issues draw commands to a {@link SpriteBatch}. */
public final class UiTreeRenderer {

    private final UiFontRegistry fonts;
    private final ResourceManagerImpl resources;
    private final UiWidgetRegistryImpl customWidgets;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private TextureRegion whitePixel;

    public UiTreeRenderer(UiFontRegistry fonts, ResourceManagerImpl resources) {
        this(fonts, resources, null);
    }

    public UiTreeRenderer(
            UiFontRegistry fonts, ResourceManagerImpl resources, UiWidgetRegistryImpl customWidgets) {
        this.fonts = Objects.requireNonNull(fonts, "fonts");
        this.resources = Objects.requireNonNull(resources, "resources");
        this.customWidgets = customWidgets;
    }

    /** Ordered draw-op ids for tests ({@code type:nodeId}). */
    public List<String> debugOps(UiNode root, UiLayoutResult layout) {
        List<String> ops = new ArrayList<>();
        walk(root, layout, ops);
        return List.copyOf(ops);
    }

    public void draw(UiNode root, UiLayoutResult layout, SpriteBatch batch, Function<String, Object> bindings) {
        Function<String, Object> resolve = bindings == null ? key -> null : bindings;
        drawTree(root, layout, Objects.requireNonNull(batch, "batch"), resolve);
    }

    void disposeWhitePixel() {
        if (whitePixel != null) {
            whitePixel.getTexture().dispose();
            whitePixel = null;
        }
    }

    private TextureRegion whitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            whitePixel = new TextureRegion(texture);
        }
        return whitePixel;
    }

    private TextureRegion texture(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            return whitePixel();
        }
        ResourceRef ref = ResourceRef.of(assetPath);
        resources.loadSync(ref, ResourceKind.TEXTURE);
        return ResourceAccess.textureRegion(resources, ref);
    }

    private void drawTree(
            UiNode node, UiLayoutResult layout, SpriteBatch batch, Function<String, Object> bindings) {
        drawNode(node, layout, batch, bindings);
        for (UiNode child : node.children()) {
            drawTree(child, layout, batch, bindings);
        }
    }

    private void walk(UiNode node, UiLayoutResult layout, List<String> ops) {
        recordOp(node, layout, ops);
        for (UiNode child : node.children()) {
            walk(child, layout, ops);
        }
    }

    private void recordOp(UiNode node, UiLayoutResult layout, List<String> ops) {
        String id = node.id();
        if (id == null || id.isBlank() || !layout.boundsById().containsKey(id)) {
            return;
        }
        if (customWidgets != null) {
            UiCustomWidgetImpl handler = customWidgets.handler(node.type());
            if (handler != null) {
                handler.recordDebugOp(node, ops);
                return;
            }
        }
        ops.add(node.type() + ":" + id);
    }

    private void drawNode(
            UiNode node, UiLayoutResult layout, SpriteBatch batch, Function<String, Object> bindings) {
        String id = node.id();
        if (id == null || id.isBlank() || !layout.boundsById().containsKey(id)) {
            return;
        }
        Rect4 bounds = layout.bounds(id);
        String type = node.type();
        if ("panel".equals(type)) {
            drawPanel(node, bounds, batch);
        } else if ("image".equals(type)) {
            drawImage(node, bounds, batch);
        } else if ("label".equals(type)) {
            drawLabel(node, bounds, batch);
        } else if ("button".equals(type)) {
            drawButton(node, bounds, batch);
        } else if ("progressBar".equals(type)) {
            drawProgressBar(node, bounds, batch, bindings);
        } else if (customWidgets != null) {
            UiCustomWidgetImpl handler = customWidgets.handler(type);
            if (handler != null) {
                handler.draw(node, bounds, batch, bindings);
            }
        }
    }

    private void drawPanel(UiNode node, Rect4 bounds, SpriteBatch batch) {
        Color color = colorFromStyle(node, 0.12f, 0.12f, 0.14f, 0.92f);
        batch.setColor(color);
        String texturePath = texturePath(node);
        if (texturePath != null && Boolean.TRUE.equals(node.prop("nineSlice"))) {
            drawNineSlice(node, bounds, batch, texturePath);
        } else if (texturePath != null) {
            batch.draw(texture(texturePath), bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(whitePixel(), bounds.x, bounds.y, bounds.width, bounds.height);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawNineSlice(UiNode node, Rect4 bounds, SpriteBatch batch, String texturePath) {
        TextureRegion region = texture(texturePath);
        float left = floatProp(node, "sliceLeft", 8f);
        float right = floatProp(node, "sliceRight", 8f);
        float top = floatProp(node, "sliceTop", 8f);
        float bottom = floatProp(node, "sliceBottom", 8f);
        batch.draw(
                region.getTexture(),
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                left,
                bottom,
                right,
                top);
    }

    private void drawImage(UiNode node, Rect4 bounds, SpriteBatch batch) {
        String texturePath = texturePath(node);
        if (texturePath == null) {
            return;
        }
        batch.draw(texture(texturePath), bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private void drawLabel(UiNode node, Rect4 bounds, SpriteBatch batch) {
        String text = stringProp(node, "text", "");
        if (text.isEmpty()) {
            return;
        }
        BitmapFont font = fonts.resolve(stringProp(node, "font", null));
        Color color = colorFromStyle(node, 1f, 1f, 1f, 1f);
        font.setColor(color);
        glyphLayout.setText(font, text);
        float x = bounds.x;
        float y = bounds.y + (bounds.height + glyphLayout.height) * 0.5f;
        font.draw(batch, glyphLayout, x, y);
        font.setColor(Color.WHITE);
    }

    private void drawButton(UiNode node, Rect4 bounds, SpriteBatch batch) {
        Color bg = colorFromStyle(node, 0.2f, 0.45f, 0.85f, 1f);
        batch.setColor(bg);
        String texturePath = texturePath(node);
        if (texturePath != null) {
            batch.draw(texture(texturePath), bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.draw(whitePixel(), bounds.x, bounds.y, bounds.width, bounds.height);
        }
        batch.setColor(Color.WHITE);
        String text = stringProp(node, "text", "");
        if (!text.isEmpty()) {
            BitmapFont font = fonts.resolve(stringProp(node, "font", null));
            font.setColor(1f, 1f, 1f, 1f);
            glyphLayout.setText(font, text);
            float textX = bounds.x + (bounds.width - glyphLayout.width) * 0.5f;
            float textY = bounds.y + (bounds.height + glyphLayout.height) * 0.5f;
            font.draw(batch, glyphLayout, textX, textY);
            font.setColor(Color.WHITE);
        }
    }

    private void drawProgressBar(
            UiNode node, Rect4 bounds, SpriteBatch batch, Function<String, Object> bindings) {
        float max = numberBinding(node, "maxBinding", bindings, 100f);
        float value = numberBinding(node, "binding", bindings, 0f);
        float fraction = max > 0f ? Math.min(1f, Math.max(0f, value / max)) : 0f;

        batch.setColor(0.15f, 0.15f, 0.15f, 0.9f);
        batch.draw(whitePixel(), bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(0.2f, 0.75f, 0.35f, 1f);
        batch.draw(whitePixel(), bounds.x, bounds.y, bounds.width * fraction, bounds.height);
        batch.setColor(Color.WHITE);
    }

    private static float numberBinding(
            UiNode node, String key, Function<String, Object> bindings, float fallback) {
        Object bindingKey = node.prop(key);
        if (bindingKey instanceof String && bindings != null) {
            Object value = bindings.apply((String) bindingKey);
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
        }
        Object literal = node.prop(key.equals("binding") ? "value" : "max");
        if (literal instanceof Number) {
            return ((Number) literal).floatValue();
        }
        return fallback;
    }

    private static String texturePath(UiNode node) {
        Object texture = node.prop("texture");
        if (texture instanceof String) {
            String path = (String) texture;
            if (!path.isBlank()) {
                return path;
            }
        }
        Object src = node.prop("src");
        if (src instanceof String) {
            String path = (String) src;
            if (!path.isBlank()) {
                return path;
            }
        }
        return null;
    }

    private static Color colorFromStyle(UiNode node, float dr, float dg, float db, float da) {
        Object styleValue = node.prop("style");
        if (!(styleValue instanceof Map)) {
            return new Color(dr, dg, db, da);
        }
        Map<?, ?> style = (Map<?, ?>) styleValue;
        Object colorValue = style.get("color");
        if (colorValue instanceof List) {
            List<?> rgba = (List<?>) colorValue;
            if (rgba.size() >= 3) {
                float r = toFloat(rgba.get(0), dr);
                float g = toFloat(rgba.get(1), dg);
                float b = toFloat(rgba.get(2), db);
                float a = rgba.size() > 3 ? toFloat(rgba.get(3), da) : da;
                return new Color(r, g, b, a);
            }
        }
        return new Color(dr, dg, db, da);
    }

    private static float toFloat(Object value, float fallback) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return fallback;
    }

    private static String stringProp(UiNode node, String key, String fallback) {
        Object value = node.prop(key);
        if (value instanceof String) {
            String text = (String) value;
            if (!text.isBlank()) {
                return text;
            }
        }
        return fallback;
    }

    private static float floatProp(UiNode node, String key, float fallback) {
        Object value = node.prop(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return fallback;
    }
}
