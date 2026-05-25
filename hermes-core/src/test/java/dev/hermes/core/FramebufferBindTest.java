package dev.hermes.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.badlogic.gdx.Gdx;
import dev.hermes.core.render.FramebufferPool;
import dev.hermes.core.render.PipelineDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class FramebufferBindTest {

    private static FramebufferGlMock.RecordingGl recordingGl;

    @BeforeAll
    static void initGl() {
        TestGdx.initHeadlessGl();
        recordingGl = FramebufferGlMock.create();
        Gdx.gl20 = recordingGl.gl();
        Gdx.gl = Gdx.gl20;
    }

    @Test
    void beginPass_offscreenTargetBindsFramebuffer() {
        PipelineDocument doc =
                PipelineDocument.parse(
                        "{\n"
                                + "  \"version\": 1,\n"
                                + "  \"framebuffers\": [\n"
                                + "    { \"id\": \"sceneColor\", \"width\": 64, \"height\": 48, \"depth\": true }\n"
                                + "  ],\n"
                                + "  \"passes\": []\n"
                                + "}\n");

        FramebufferPool pool = new FramebufferPool(doc.framebuffers());
        pool.resize(800, 600);
        pool.beginPass("sceneColor");

        assertEquals("sceneColor", pool.lastBoundTarget());
        assertFalse(recordingGl.bindFramebufferCalls().isEmpty());

        pool.endPass("sceneColor");
        pool.dispose();
    }

    @Test
    void targetDimensionsReturnFramebufferSize() {
        PipelineDocument doc =
                PipelineDocument.parse(
                        "{\n"
                                + "  \"version\": 1,\n"
                                + "  \"framebuffers\": [\n"
                                + "    { \"id\": \"sceneColor\", \"width\": 64, \"height\": 48, \"depth\": true }\n"
                                + "  ],\n"
                                + "  \"passes\": []\n"
                                + "}\n");

        FramebufferPool pool = new FramebufferPool(doc.framebuffers());
        pool.resize(800, 600);

        assertEquals(64, pool.targetWidth("sceneColor"));
        assertEquals(48, pool.targetHeight("sceneColor"));
        assertEquals(800, pool.targetWidth("screen"));
        assertEquals(600, pool.targetHeight("screen"));
        pool.dispose();
    }
}
