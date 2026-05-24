package dev.hermes.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.log.LogLevel;
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
}
