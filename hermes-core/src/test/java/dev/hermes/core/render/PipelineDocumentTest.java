package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PipelineDocumentTest {

    static final String VALID_JSON =
            "{\n"
                    + "  \"version\": 1,\n"
                    + "  \"clearColor\": [0.15, 0.15, 0.2, 1],\n"
                    + "  \"framebuffers\": [\n"
                    + "    { \"id\": \"sceneColor\", \"width\": 0, \"height\": 0, \"depth\": true }\n"
                    + "  ],\n"
                    + "  \"shaders\": {\n"
                    + "    \"default/unlit\": {\n"
                    + "      \"vertex\": \"shaders/default.vert\",\n"
                    + "      \"fragment\": \"shaders/default.frag\"\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"passes\": [\n"
                    + "    { \"id\": \"world3d\", \"type\": \"world3d\", \"target\": \"screen\", \"layers\": [\"WORLD\"] },\n"
                    + "    { \"id\": \"sprites\", \"type\": \"sprites\", \"target\": \"screen\", \"layers\": [\"WORLD\"] },\n"
                    + "    { \"id\": \"ui\", \"type\": \"ui\", \"target\": \"screen\", \"layers\": [\"UI\"], \"depthTest\": false }\n"
                    + "  ]\n"
                    + "}\n";

    @Test
    void parse_readsVersionClearColorFramebuffersShadersAndPasses() {
        PipelineDocument doc = PipelineDocument.parse(VALID_JSON);

        assertEquals(1, doc.version());
        assertArrayEquals(new float[]{0.15f, 0.15f, 0.2f, 1f}, doc.clearColor(), 0.0001f);

        assertEquals(1, doc.framebuffers().size());
        assertEquals("sceneColor", doc.framebuffers().get(0).id());
        assertEquals(0, doc.framebuffers().get(0).width());
        assertEquals(0, doc.framebuffers().get(0).height());
        assertTrue(doc.framebuffers().get(0).depth());

        assertEquals(1, doc.shaders().size());
        PipelineDocument.ShaderDef shader = doc.shaders().get("default/unlit");
        assertEquals("shaders/default.vert", shader.vertex());
        assertEquals("shaders/default.frag", shader.fragment());

        assertEquals(3, doc.passes().size());
        assertEquals("world3d", doc.passes().get(0).id());
        assertEquals(PipelineDocument.PassType.WORLD3D, doc.passes().get(0).type());
        assertEquals("screen", doc.passes().get(0).target());
        assertEquals(java.util.List.of("WORLD"), doc.passes().get(0).layers());
        assertTrue(doc.passes().get(0).depthTest());

        assertEquals("ui", doc.passes().get(2).id());
        assertEquals(PipelineDocument.PassType.UI, doc.passes().get(2).type());
        assertEquals(false, doc.passes().get(2).depthTest());
    }

    @Test
    void parse_unknownPassTypeThrows() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [{ \"id\": \"bad\", \"type\": \"raytrace\", \"target\": \"screen\", \"layers\": [\"WORLD\"] }]\n"
                        + "}\n";

        PipelineParseException error =
                assertThrows(PipelineParseException.class, () -> PipelineDocument.parse(json));

        assertTrue(error.getMessage().contains("raytrace"));
    }

    @Test
    void parse_futurePassTypes_postParticlesCompute() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    { \"id\": \"post\", \"type\": \"post\", \"target\": \"screen\" },\n"
                        + "    { \"id\": \"fx\", \"type\": \"particles\", \"target\": \"screen\" },\n"
                        + "    { \"id\": \"cull\", \"type\": \"compute\", \"target\": \"screen\" }\n"
                        + "  ]\n"
                        + "}\n";

        PipelineDocument doc = PipelineDocument.parse(json);

        assertEquals(PipelineDocument.PassType.POST, doc.passes().get(0).type());
        assertEquals(PipelineDocument.PassType.PARTICLES, doc.passes().get(1).type());
        assertEquals(PipelineDocument.PassType.COMPUTE, doc.passes().get(2).type());
    }

    @Test
    void parse_uiPassOptionalCamera() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"passes\": [\n"
                        + "    {\n"
                        + "      \"id\": \"ui\",\n"
                        + "      \"type\": \"ui\",\n"
                        + "      \"target\": \"screen\",\n"
                        + "      \"layers\": [\"UI\"],\n"
                        + "      \"camera\": \"ui-camera\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        PipelineDocument doc = PipelineDocument.parse(json);

        assertEquals("ui-camera", doc.passes().get(0).camera());
    }

    @Test
    void parse_unsupportedVersionThrows() {
        String json = "{ \"version\": 99, \"passes\": [] }";

        PipelineParseException error =
                assertThrows(PipelineParseException.class, () -> PipelineDocument.parse(json));

        assertTrue(error.getMessage().contains("version"));
    }
}
