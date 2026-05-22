package dev.hermes.gradle.dsl;

import java.util.Locale;

public class LoggingExtension {

  private String minLevel;

  public String getMinLevel() {
    return minLevel;
  }

  public void setMinLevel(String minLevel) {
    this.minLevel = minLevel;
  }

  public String resolveMinLevel(boolean debug, boolean export) {
    if (minLevel != null && !minLevel.isBlank()) {
      return minLevel.trim().toUpperCase(Locale.ROOT);
    }
    if (export) {
      return "WARN";
    }
    if (debug) {
      return "DEBUG";
    }
    return null;
  }
}