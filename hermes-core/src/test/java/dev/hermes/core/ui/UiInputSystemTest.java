package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ui.UiAnchor;
import dev.hermes.api.ui.UiLayout;
import dev.hermes.api.ui.UiNode;
import org.junit.jupiter.api.Test;

final class UiInputSystemTest {

    @Test
    void clickOnButtonFiresAction() {
        UiNode root = UiNode.button("play", "Play", "start_game")
                .layout(UiLayout.of(UiAnchor.CENTER, 100, 40));
        UiLayoutResult layout = new UiLayoutEngine().layout(root, 800, 600, 1f);
        RecordingUiCallbacks cb = new RecordingUiCallbacks();
        new UiInputSystem(cb).click(layout, root, 400, 300);
        assertEquals("start_game", cb.lastAction());
    }

    static final class RecordingUiCallbacks implements UiActionPulse {
        private String lastAction;

        @Override
        public void pulseAction(String action) {
            lastAction = action;
        }

        String lastAction() {
            return lastAction;
        }
    }
}
