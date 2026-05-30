package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ui.UiAnchor;
import dev.hermes.api.ui.UiLayout;
import dev.hermes.api.ui.UiNode;
import org.junit.jupiter.api.Test;

final class UiLayoutEngineTest {

    @Test
    void centerAnchorPlacesChildInMiddle() {
        UiNode root = UiNode.panel("root").layout(UiLayout.stretch());
        UiNode child = UiNode.label("lbl", "Hi").layout(UiLayout.of(UiAnchor.CENTER, 100, 40));
        root.addChild(child);

        UiLayoutResult result = new UiLayoutEngine().layout(root, 800, 600, 1f);
        assertEquals(350f, result.bounds("lbl").x(), 0.01f);
        assertEquals(280f, result.bounds("lbl").y(), 0.01f);
    }
}
