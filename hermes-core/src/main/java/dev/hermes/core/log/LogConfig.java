package dev.hermes.core.log;

import dev.hermes.api.log.LogLevel;
import dev.hermes.core.HermesRuntimeConfig;

final class LogConfig {

  private static final int MIN_SEVERITY = resolveMinSeverity();

  private LogConfig() {}

  static boolean isEnabled(LogLevel level) {
    return level.severity() >= MIN_SEVERITY;
  }

  static int minSeverity() {
    return MIN_SEVERITY;
  }

  private static int resolveMinSeverity() {
    String explicit = HermesRuntimeConfig.get("hermes.log.minLevel", "");
    if (!explicit.isBlank()) {
      return LogLevel.parse(explicit).severity();
    }
    boolean debug = Boolean.parseBoolean(HermesRuntimeConfig.get("hermes.debug", "false"));
    return (debug ? LogLevel.DEBUG : LogLevel.INFO).severity();
  }
}