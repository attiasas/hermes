package dev.hermes.gradle.dsl;

import java.util.Locale;
import java.util.List;

public class LoggingExtension {

    private String minLevel;

    private String patternType;

    private List<String> patterns;

    public String getMinLevel() {
        return minLevel;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
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
        return "INFO";
    }
}