package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class HermesEngineImplAudioTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void audioServiceIsAvailable() {
        HermesEngineImpl engine = new HermesEngineImpl();
        assertNotNull(engine.audio());
    }
}
