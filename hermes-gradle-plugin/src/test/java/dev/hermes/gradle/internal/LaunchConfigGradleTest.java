package dev.hermes.gradle.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.dsl.LoggingExtension;
import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.launch.LaunchMode;
import dev.hermes.tooling.launch.RuntimeConfigKeys;
import dev.hermes.tooling.platform.Platforms;
import java.util.List;
import org.junit.jupiter.api.Test;

final class LaunchConfigGradleTest {

    @Test
    void resolveFromParts_includesLoggingForHtmlPath() {
        HermesExtension extension = new HermesExtension();
        extension.setDebug(true);
        LoggingExtension logging = extension.getLogging();
        logging.setMinLevel("ERROR");
        logging.setPatternType("WILDCARD");
        logging.setPatterns(List.of("*Scene*"));

        HermesGameConfig game = new HermesGameConfig();
        Platforms platforms = new Platforms();

        var props = LaunchConfigGradle.resolveFromParts(extension, game, platforms, LaunchMode.DEV).asMap();

        assertEquals("ERROR", props.get(RuntimeConfigKeys.LOG_MIN_LEVEL));
        assertEquals("*Scene*", props.get(RuntimeConfigKeys.LOG_PATTERNS));
    }
}
