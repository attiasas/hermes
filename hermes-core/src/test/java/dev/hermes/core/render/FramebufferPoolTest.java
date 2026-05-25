package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

final class FramebufferPoolTest {

    @Test
    void resize_zeroDimensionsUseWindowSize() {
        PipelineDocument doc =
                PipelineDocument.parse(
                        "{\n"
                                + "  \"version\": 1,\n"
                                + "  \"framebuffers\": [\n"
                                + "    { \"id\": \"sceneColor\", \"width\": 0, \"height\": 0, \"depth\": true },\n"
                                + "    { \"id\": \"hud\", \"width\": 320, \"height\": 180, \"depth\": false }\n"
                                + "  ],\n"
                                + "  \"passes\": []\n"
                                + "}\n");

        FramebufferPool pool = new FramebufferPool(doc.framebuffers(), false);
        pool.resize(1280, 720);

        assertEquals(List.of("sceneColor", "hud"), pool.allocationOrder());
        pool.dispose();
    }

    @Test
    void duplicateFramebufferIdThrows() {
        PipelineDocument doc =
                PipelineDocument.parse(
                        "{\n"
                                + "  \"version\": 1,\n"
                                + "  \"framebuffers\": [\n"
                                + "    { \"id\": \"dup\", \"width\": 64, \"height\": 64, \"depth\": false },\n"
                                + "    { \"id\": \"dup\", \"width\": 32, \"height\": 32, \"depth\": false }\n"
                                + "  ],\n"
                                + "  \"passes\": []\n"
                                + "}\n");

        assertThrows(
                PipelineParseException.class, () -> new FramebufferPool(doc.framebuffers(), false));
    }
}
