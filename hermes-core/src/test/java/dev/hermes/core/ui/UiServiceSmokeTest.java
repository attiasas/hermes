package dev.hermes.core.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.HermesEngineImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class UiServiceSmokeTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void engineExposesUiService() {
        HermesEngineImpl engine = new HermesEngineImpl();
        assertNotNull(engine.ui());
    }
}
