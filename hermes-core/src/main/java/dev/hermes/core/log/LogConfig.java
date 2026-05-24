package dev.hermes.core.log;

import java.util.List;

import dev.hermes.api.log.LogLevel;
import dev.hermes.core.HermesRuntimeConfig;
import dev.hermes.core.utils.StringMatcher;
import dev.hermes.core.utils.StringMatcher.MatchType;

final class LogConfig {

    private static final int MIN_SEVERITY = resolveMinSeverity();
    private static final StringMatcher MATCHER = resolveMatcher();

    private LogConfig() {
    }

    static boolean isEnabled(LogLevel level) {
        return level.severity() >= MIN_SEVERITY;
    }

    static boolean isMatched(String category) {
        if (MATCHER == null) {
            return true;
        }
        return MATCHER.matches(category);
    }

    static int minSeverity() {
        return MIN_SEVERITY;
    }

    private static StringMatcher resolveMatcher() {
        String patterns = HermesRuntimeConfig.get("hermes.log.patterns", "");
        if (!patterns.isBlank()) {
            MatchType type = MatchType.valueOf(HermesRuntimeConfig.get("hermes.log.patternType", "WILDCARD"));
            return new StringMatcher(type == null ? MatchType.WILDCARD : type, List.of(patterns.split(";")));
        }
        return null;
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