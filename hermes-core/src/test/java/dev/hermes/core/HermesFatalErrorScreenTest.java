package dev.hermes.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.core.render.ShaderCompileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class HermesFatalErrorScreenTest {

    private HermesFatalErrorScreen screen;

    @BeforeEach
    void setUp() {
        TestGdx.initHeadlessGl();
        screen = new HermesFatalErrorScreen();
    }

    @AfterEach
    void tearDown() {
        screen.dispose();
    }

    @Test
    void inactive_beforeReport() {
        assertFalse(screen.isActive());
    }

    @Test
    void report_marksActive() {
        screen.report(
                new ShaderCompileException(
                        "failed to compile shader 'water': Precisions of uniform 'u_time' differ"));
        assertTrue(screen.isActive());
    }
}
