package dev.hermes.core.render.resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class WaterShaderPrecisionTest {

    private static Path shaderResource(String name) throws Exception {
        URL url = WaterShaderPrecisionTest.class.getResource("/assets/shaders/" + name);
        assertNotNull(url, "missing test resource /assets/shaders/" + name);
        return Path.of(url.toURI());
    }

    @Test
    void waterVertex_declaresMediumpFloatBeforeUtime_onGles() throws Exception {
        String source = Files.readString(shaderResource("water.vert"));
        int precisionIdx = source.indexOf("precision mediump float");
        int utimeIdx = source.indexOf("uniform mediump float u_time");
        assertTrue(precisionIdx >= 0, "water.vert must declare precision mediump float for GL_ES");
        assertTrue(utimeIdx >= 0, "water.vert must declare u_time as mediump on GLES");
        assertTrue(precisionIdx < utimeIdx, "precision must precede u_time uniform");
    }

    @Test
    void waterShaders_useMatchingMediumpUtime_onGles() throws Exception {
        String vertSource = Files.readString(shaderResource("water.vert"));
        String fragSource = Files.readString(shaderResource("water.frag"));

        assertTrue(vertSource.contains("uniform mediump float u_time"), "vertex u_time must be mediump on GLES");
        assertTrue(fragSource.contains("uniform mediump float u_time"), "fragment u_time must be mediump on GLES");
    }
}
