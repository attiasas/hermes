package dev.hermes.gradle.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class LoggingExtensionTest {

  @Test
  void resolveMinLevel_explicitWins() {
    LoggingExtension logging = new LoggingExtension();
    logging.setMinLevel("ERROR");
    assertEquals("ERROR", logging.resolveMinLevel(true, false));
  }

  @Test
  void resolveMinLevel_exportDefaultsWarn() {
    LoggingExtension logging = new LoggingExtension();
    assertEquals("WARN", logging.resolveMinLevel(true, true));
  }

  @Test
  void resolveMinLevel_debugBuildDefaultsDebug() {
    LoggingExtension logging = new LoggingExtension();
    assertEquals("DEBUG", logging.resolveMinLevel(true, false));
  }

  @Test
  void resolveMinLevel_releaseRunDefaultsInfo() {
    LoggingExtension logging = new LoggingExtension();
    assertEquals("INFO", logging.resolveMinLevel(false, false));
  }
}