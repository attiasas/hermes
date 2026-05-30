package dev.hermes.core.ui;

import dev.hermes.api.math.Rect4;
import dev.hermes.api.ui.UiNode;
import dev.hermes.api.ui.UiWidgetRegistration;
import dev.hermes.api.ui.UiWidgetRegistry;
import java.util.List;
import java.util.function.Function;

/** Test SPI entry registering a minimal {@code badge} widget type. */
public final class TestBadgeWidgetRegistration implements UiWidgetRegistration {

    @Override
    public void register(UiWidgetRegistry registry) {
        registry.register(
                "badge",
                new UiCustomWidgetImpl() {
                    @Override
                    public void recordDebugOp(UiNode node, List<String> ops) {
                        String id = node.id();
                        if (id != null && !id.isBlank()) {
                            ops.add("badge:" + id);
                        }
                    }

                    @Override
                    public void draw(
                            UiNode node,
                            Rect4 bounds,
                            com.badlogic.gdx.graphics.g2d.SpriteBatch batch,
                            Function<String, Object> bindings) {
                        // No-op for unit tests; debug ops cover registration wiring.
                    }
                });
    }
}
