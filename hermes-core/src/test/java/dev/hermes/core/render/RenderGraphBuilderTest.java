package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class RenderGraphBuilderTest {

  @Test
  void build_createsPassOrderMatchingDocument() {
    PipelineDocument doc = PipelineDocument.parse(PipelineDocumentTest.VALID_JSON);

    RenderGraph graph = new RenderGraphBuilder().buildWithStubs(doc);

    assertArrayEquals(new float[] {0.15f, 0.15f, 0.2f, 1f}, graph.clearColor(), 0.0001f);
    assertEquals(3, graph.passCount());
    assertEquals("world3d", graph.passId(0));
    assertEquals("sprites", graph.passId(1));
    assertEquals("ui", graph.passId(2));
    graph.dispose();
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
