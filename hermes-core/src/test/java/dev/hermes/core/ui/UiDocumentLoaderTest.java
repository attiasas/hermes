package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.ui.UiDocument;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class UiDocumentLoaderTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void loadsPanelWithLabelChild() {
        UiWidgetRegistryImpl registry = new UiWidgetRegistryImpl();
        UiDocument doc =
                new UiDocumentLoader(new UiWidgetTypes(new BuiltinUiWidgets(), registry))
                        .load("ui/test-panel.json");
        assertEquals(800, doc.designWidth());
        assertEquals("label", doc.root().children().get(0).type());
        assertEquals("Hello", doc.root().children().get(0).prop("text"));
    }
}
