package dev.hermes.api.log;

import java.util.Locale;

public enum LogLevel {
  DEBUG(10),
  INFO(20),
  WARN(30),
  ERROR(40);

  private final int severity;

  LogLevel(int severity) {
    this.severity = severity;
  }

  public int severity() {
    return severity;
  }

  public static LogLevel parse(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("log level name is required");
    }
    return LogLevel.valueOf(name.trim().toUpperCase(Locale.ROOT));
  }
}