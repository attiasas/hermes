package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ui.UiNode;
import dev.hermes.core.resource.ResourceManagerImpl;
import java.util.List;
import org.junit.jupiter.api.Test;

final class UiTreeRendererTest {

    @Test
    void collectDrawCommandsForLabel() {
        UiNode root = UiNode.label("t", "Hello");
        UiLayoutResult layout = new UiLayoutEngine().layout(root, 200, 100, 1f);
        UiTreeRenderer renderer =
                new UiTreeRenderer(new UiFontRegistry(), ResourceManagerImpl.createDefault());
        assertEquals(List.of("label:t"), renderer.debugOps(root, layout));
    }
}
