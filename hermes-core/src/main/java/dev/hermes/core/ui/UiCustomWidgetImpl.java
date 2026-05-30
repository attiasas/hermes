package dev.hermes.core.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.ui.UiCustomWidget;
import dev.hermes.api.ui.UiNode;
import java.util.List;
import java.util.function.Function;

/** Core-side custom widget hooks (deserialize props are already on {@link UiNode}). */
public interface UiCustomWidgetImpl extends UiCustomWidget {

    void recordDebugOp(UiNode node, List<String> ops);

    void draw(UiNode node, Rect4 bounds, SpriteBatch batch, Function<String, Object> bindings);
}
