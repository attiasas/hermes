package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.ecs.World;
import dev.hermes.api.render.RenderContext;
import dev.hermes.api.render.RenderPass;
import dev.hermes.api.render.RenderPassRegistry;
import org.junit.jupiter.api.Test;

final class CustomRenderPassRegistrationTest {

    @Test
    void parse_customPassReadsHandler() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"water\", \"type\": \"custom\", \"handler\": \"water\", \"target\": \"screen\" }\n"
                        + "  ]\n"
                        + "}\n";

        PipelineDocument doc = PipelineDocument.parse(json);

        assertEquals(1, doc.passes().size());
        assertEquals(PipelineDocument.PassType.CUSTOM, doc.passes().get(0).type());
        assertEquals("water", doc.passes().get(0).handler());
    }

    @Test
    void build_resolvesRegisteredCustomHandler() {
        RenderPassRegistry registry = new RenderPassRegistry();
        registry.register("water", new RecordingPass());

        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"water\", \"type\": \"custom\", \"handler\": \"water\", \"target\": \"screen\" }\n"
                        + "  ]\n"
                        + "}\n";

        RenderGraph graph =
                new RenderGraphBuilder(registry).buildWithStubs(PipelineDocument.parse(json));

        assertEquals(1, graph.passCount());
        assertEquals("water", graph.passId(0));
        graph.dispose();
    }

    @Test
    void build_unregisteredCustomHandlerThrows() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"water\", \"type\": \"custom\", \"handler\": \"water\", \"target\": \"screen\" }\n"
                        + "  ]\n"
                        + "}\n";

        PipelineParseException error =
                assertThrows(
                        PipelineParseException.class,
                        () -> new RenderGraphBuilder().buildWithStubs(PipelineDocument.parse(json)));

        assertTrue(error.getMessage().contains("water"));
        assertTrue(error.getMessage().contains("unregistered"));
    }

    @Test
    void parse_customPassWithoutHandlerThrows() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [{ \"id\": \"water\", \"type\": \"custom\", \"target\": \"screen\" }]\n"
                        + "}\n";

        PipelineParseException error =
                assertThrows(PipelineParseException.class, () -> PipelineDocument.parse(json));

        assertTrue(error.getMessage().contains("handler"));
    }

    private static final class RecordingPass implements RenderPass {
        @Override
        public void resize(int width, int height) {
        }

        @Override
        public void render(World world, RenderContext context) {
        }

        @Override
        public void dispose() {
        }
    }
}
