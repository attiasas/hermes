package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ui.UiDocument;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class UiWidgetRegistrationTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void serviceLoaderRegistersCustomBadgeWidget() {
        HermesEngineImpl engine = new HermesEngineImpl();
        assertTrue(engine.ui().widgets().supports("badge"));
    }

    @Test
    void badgeWidgetAppearsInDrawOps() {
        HermesEngineImpl engine = new HermesEngineImpl();
        UiServiceImpl ui = (UiServiceImpl) engine.ui();
        UiDocument doc = ui.load("ui/test-badge.json");
        UiLayoutResult layout =
                new UiLayoutEngine().layout(doc.root(), 100, 50, 1f);
        UiWidgetRegistryImpl widgets = (UiWidgetRegistryImpl) ui.widgets();
        UiTreeRenderer renderer = new UiTreeRenderer(new UiFontRegistry(), new UiTextureCache(), widgets);
        assertEquals(List.of("badge:b1"), renderer.debugOps(doc.root(), layout));
    }
}
