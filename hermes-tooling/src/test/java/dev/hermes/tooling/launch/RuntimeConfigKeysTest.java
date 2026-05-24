package dev.hermes.tooling.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class RuntimeConfigKeysTest {

    @Test
    void logKeys_useHermesPrefix() {
        assertEquals("hermes.log.minLevel", RuntimeConfigKeys.LOG_MIN_LEVEL);
        assertEquals("hermes.log.patternType", RuntimeConfigKeys.LOG_PATTERN_TYPE);
        assertEquals("hermes.log.patterns", RuntimeConfigKeys.LOG_PATTERNS);
    }
}
