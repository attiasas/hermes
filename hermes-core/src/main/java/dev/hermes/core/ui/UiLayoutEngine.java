package dev.hermes.core.ui;

import dev.hermes.api.math.Rect4;
import dev.hermes.api.ui.UiAnchor;
import dev.hermes.api.ui.UiLayout;
import dev.hermes.api.ui.UiNode;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes widget bounds in SURFACE pixels from a tree authored in design resolution.
 */
public final class UiLayoutEngine {

    public UiLayoutResult layout(UiNode root, int designWidth, int designHeight, float scale) {
        Map<String, Rect4> boundsById = new HashMap<>();
        Rect4 designRoot = new Rect4(0f, 0f, designWidth, designHeight);
        layoutPostOrder(root, designRoot, scale, boundsById);
        return new UiLayoutResult(boundsById);
    }

    private void layoutPostOrder(UiNode node, Rect4 parentDesign, float scale, Map<String, Rect4> out) {
        Rect4 designBounds = computeDesignBounds(node, parentDesign);
        for (UiNode child : node.children()) {
            layoutPostOrder(child, designBounds, scale, out);
        }
        String id = node.id();
        if (id != null && !id.isBlank()) {
            out.put(id, toSurface(designBounds, scale));
        }
    }

    private static Rect4 computeDesignBounds(UiNode node, Rect4 parent) {
        UiLayout layout = node.layout();
        Rect4 content = contentArea(parent, layout);
        float width = resolveSize(layout.width(), content.width);
        float height = resolveSize(layout.height(), content.height);
        float x = placeX(layout, content, width);
        float y = placeY(layout, content, height);
        if (layout.anchor() == UiAnchor.STRETCH) {
            return new Rect4(content.x, content.y, content.width, content.height);
        }
        return new Rect4(x, y, width, height);
    }

    private static Rect4 contentArea(Rect4 parent, UiLayout layout) {
        return new Rect4(
                parent.x + layout.paddingLeft(),
                parent.y + layout.paddingTop(),
                parent.width - layout.paddingLeft() - layout.paddingRight(),
                parent.height - layout.paddingTop() - layout.paddingBottom());
    }

    private static float resolveSize(float layoutSize, float parentSize) {
        if (Float.isNaN(layoutSize) || layoutSize <= 0f) {
            return parentSize;
        }
        return layoutSize;
    }

    private static float placeX(UiLayout layout, Rect4 content, float childWidth) {
        float offsetX = layout.offsetX();
        UiAnchor anchor = layout.anchor();
        if (anchor == UiAnchor.TOP_CENTER || anchor == UiAnchor.CENTER || anchor == UiAnchor.BOTTOM_CENTER) {
            return content.x + (content.width - childWidth) * 0.5f + offsetX;
        }
        if (anchor == UiAnchor.TOP_RIGHT || anchor == UiAnchor.CENTER_RIGHT || anchor == UiAnchor.BOTTOM_RIGHT) {
            return content.x + content.width - childWidth - offsetX;
        }
        return content.x + offsetX;
    }

    private static float placeY(UiLayout layout, Rect4 content, float childHeight) {
        float offsetY = layout.offsetY();
        UiAnchor anchor = layout.anchor();
        if (anchor == UiAnchor.CENTER_LEFT || anchor == UiAnchor.CENTER || anchor == UiAnchor.CENTER_RIGHT) {
            return content.y + (content.height - childHeight) * 0.5f + offsetY;
        }
        if (anchor == UiAnchor.BOTTOM_LEFT || anchor == UiAnchor.BOTTOM_CENTER || anchor == UiAnchor.BOTTOM_RIGHT) {
            return content.y + content.height - childHeight - offsetY;
        }
        return content.y + offsetY;
    }

    private static Rect4 toSurface(Rect4 design, float scale) {
        return new Rect4(
                design.x * scale, design.y * scale, design.width * scale, design.height * scale);
    }
}
