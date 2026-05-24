package dev.hermes.core.log;

import dev.hermes.api.log.LogLevel;
import dev.hermes.core.config.RuntimeConfigServices;
import dev.hermes.core.utils.StringMatcher;
import dev.hermes.core.utils.StringMatcher.MatchType;
import java.util.List;

final class LogConfig {

    private static volatile int minSeverity = -1;
    private static volatile StringMatcher matcher;
    private static volatile boolean matcherResolved;

    private LogConfig() {}

    static boolean isEnabled(LogLevel level) {
        ensureInitialized();
        return level.severity() >= minSeverity;
    }

    static boolean isMatched(String category) {
        ensureInitialized();
        if (matcher == null) {
            return true;
        }
        return matcher.matches(category);
    }

    static int minSeverity() {
        ensureInitialized();
        return minSeverity;
    }

    static void reinitialize() {
        minSeverity = -1;
        matcher = null;
        matcherResolved = false;
    }

    private static void ensureInitialized() {
        if (minSeverity >= 0 && matcherResolved) {
            return;
        }
        synchronized (LogConfig.class) {
            if (minSeverity >= 0 && matcherResolved) {
                return;
            }
            minSeverity = RuntimeConfigServices.get().logMinSeverity();
            String patterns = RuntimeConfigServices.get().logPatterns();
            if (patterns != null && !patterns.isBlank()) {
                MatchType type = MatchType.valueOf(RuntimeConfigServices.get().logPatternType());
                matcher = new StringMatcher(type, List.of(patterns.split(";")));
            } else {
                matcher = null;
            }
            matcherResolved = true;
        }
    }
}
