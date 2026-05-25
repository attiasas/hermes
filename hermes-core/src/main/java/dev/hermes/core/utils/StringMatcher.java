package dev.hermes.core.utils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringMatcher {

    public enum MatchType {
        WILDCARD,
        REGEX
    }

    private final List<Pattern> patterns;

    public StringMatcher(MatchType type, List<String> patterns) {
        switch (type) {
            case WILDCARD:
                this.patterns = patterns.stream().map(p -> Pattern.compile(wildcardToRegex(p))).collect(Collectors.toList());
                break;
            case REGEX:
                this.patterns = patterns.stream().map(Pattern::compile).collect(Collectors.toList());
                break;
            default:
                throw new IllegalArgumentException("Invalid match type: " + type);
        }
        ;
    }

    /**
     * Main entry point.
     * <p>
     * Rules:
     * - null/empty patterns => allow all
     * - any matching pattern => true
     */
    public boolean matches(String value) {

        if (value == null) {
            return false;
        }

        if (patterns == null || patterns.isEmpty()) {
            return true;
        }

        return patterns.stream().anyMatch(pattern -> matchesSingle(value, pattern));
    }

    private boolean matchesSingle(String value, Pattern pattern) {
        if (value == null || pattern == null) {
            return false;
        }
        return pattern.matcher(value).matches();
    }

    private String wildcardToRegex(String wildcard) {
        if (wildcard == null || wildcard.isBlank()) {
            return "";
        }
        return "^" +
                wildcard
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".")
                + "$";
    }
}
