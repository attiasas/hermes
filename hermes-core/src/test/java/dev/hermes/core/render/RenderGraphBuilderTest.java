package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

final class RenderGraphBuilderTest {

    @Test
    void build_createsPassOrderMatchingDocument() {
        PipelineDocument doc = PipelineDocument.parse(PipelineDocumentTest.VALID_JSON);

        RenderGraph graph = new RenderGraphBuilder().buildWithStubs(doc);

        assertArrayEquals(new float[]{0.15f, 0.15f, 0.2f, 1f}, graph.clearColor(), 0.0001f);
        assertEquals(3, graph.passCount());
        assertEquals("world3d", graph.passId(0));
        assertEquals("sprites", graph.passId(1));
        assertEquals("ui", graph.passId(2));
        graph.dispose();
    }

    @Test
    void build_allocatesFramebuffersBeforePassesThatTargetThem() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"framebuffers\": [\n"
                        + "    { \"id\": \"sceneColor\", \"width\": 0, \"height\": 0, \"depth\": true }\n"
                        + "  ],\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"world3d\", \"type\": \"world3d\", \"target\": \"sceneColor\", \"layers\": [\"WORLD\"] },\n"
                        + "    { \"id\": \"ui\", \"type\": \"ui\", \"target\": \"screen\", \"layers\": [\"UI\"] }\n"
                        + "  ]\n"
                        + "}\n";

        PipelineDocument doc = PipelineDocument.parse(json);
        RenderGraph graph = new RenderGraphBuilder().buildWithStubs(doc);
        graph.resize(800, 600);

        assertEquals(List.of("sceneColor"), graph.framebufferPool().allocationOrder());
        assertEquals("sceneColor", graph.passTarget(0));
        assertEquals("screen", graph.passTarget(1));
        graph.dispose();
    }

    @Test
    void build_screenTargetRecordsBinding() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"ui\", \"type\": \"ui\", \"target\": \"screen\", \"layers\": [\"UI\"] }\n"
                        + "  ]\n"
                        + "}\n";

        RenderGraph graph = new RenderGraphBuilder().buildWithStubs(PipelineDocument.parse(json));
        graph.resize(640, 480);
        graph.render(null);

        assertEquals("screen", graph.framebufferPool().lastBoundTarget());
        graph.dispose();
    }

    @Test
    void build_unknownFramebufferTargetThrows() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [{ \"id\": \"bad\", \"type\": \"sprites\", \"target\": \"missing\", \"layers\": [\"WORLD\"] }]\n"
                        + "}\n";

        PipelineParseException error =
                assertThrows(
                        PipelineParseException.class,
                        () -> new RenderGraphBuilder().buildWithStubs(PipelineDocument.parse(json)));

        assertTrue(error.getMessage().contains("missing"));
    }

    @Test
    void build_unknownRenderLayerThrows() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [{ \"id\": \"bad\", \"type\": \"sprites\", \"target\": \"screen\", \"layers\": [\"HUD\"] }]\n"
                        + "}\n";

        PipelineParseException error =
                assertThrows(
                        PipelineParseException.class,
                        () -> new RenderGraphBuilder().buildWithStubs(PipelineDocument.parse(json)));

        assertTrue(error.getMessage().contains("HUD"));
    }
}
