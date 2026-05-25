package dev.hermes.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.log.LogLevel;
import dev.hermes.core.HermesRuntimeConfig;
import dev.hermes.core.TestGdx;
import dev.hermes.core.log.LoggingRuntime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class RuntimeConfigServiceImplTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("hermes.log.minLevel");
        RuntimeConfigServices.install(new RuntimeConfigServiceImpl());
        LoggingRuntime.reinitialize();
    }

    @Test
    void logMinLevel_readsFromSystemProperty() {
        System.setProperty("hermes.log.minLevel", "ERROR");
        RuntimeConfigServices.install(new RuntimeConfigServiceImpl());
        LoggingRuntime.reinitialize();

        RuntimeConfigServiceImpl service = new RuntimeConfigServiceImpl();
        assertEquals("ERROR", service.logMinLevel());
        assertEquals(LogLevel.ERROR.severity(), service.logMinSeverity());
        assertTrue(service.logMinSeverity() > LogLevel.WARN.severity());
    }

    @Test
    void logMinLevel_readsPackagedAssetAfterReload() {
        TestGdx.initClasspathFiles();
        HermesRuntimeConfig.reload();
        RuntimeConfigServices.install(new RuntimeConfigServiceImpl());
        LoggingRuntime.reinitialize();

        assertEquals("WARN", RuntimeConfigServices.get().logMinLevel());
        assertEquals("*SceneStack*", RuntimeConfigServices.get().logPatterns());
    }
}
