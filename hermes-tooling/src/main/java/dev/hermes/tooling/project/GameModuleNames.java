package dev.hermes.tooling.project;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GameModuleNames {

    private static final Pattern VALID = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");
    private static final Pattern SETTINGS_GAME_MODULE =
            Pattern.compile("gameModule\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Set<String> RESERVED = Set.of("hermes-api", "hermes-core", "hermes-cli");

    private GameModuleNames() {}

    /** CLI default when {@code --module} is omitted. */
    public static String defaultName() {
        return "game";
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return defaultName();
        }
        String name = raw.trim();
        validate(name);
        return name;
    }

    public static void validate(String name) {
        if (name.length() > 64) {
            throw new IllegalArgumentException("Module name too long: " + name);
        }
        if (!VALID.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid module name '"
                            + name
                            + "'. Use letters, digits, hyphens, underscores; start with a letter.");
        }
        if (name.startsWith("hermes-launcher-")) {
            throw new IllegalArgumentException("Module name must not use hermes-launcher- prefix: " + name);
        }
        if (RESERVED.contains(name)) {
            throw new IllegalArgumentException("Module name is reserved: " + name);
        }
    }

    /** Parse {@code hermes { gameModule = '...' }} from settings.gradle (standalone doctor). */
    public static String parseFromSettingsGradle(String content) {
        Matcher matcher = SETTINGS_GAME_MODULE.matcher(content);
        if (matcher.find()) {
            return normalize(matcher.group(1));
        }
        return defaultName();
    }

    /**
     * When materializing templates, rename paths under the default module directory ({@code game})
     * to a custom {@code gameModule} name. Normalizes Windows backslashes first.
     */
    public static String remapTemplatePath(String relativePath, String gameModule) {
        String normalized = relativePath.replace('\\', '/');
        String target = normalize(gameModule);
        if (defaultName().equals(target)) {
            return normalized;
        }
        if (defaultName().equals(normalized)) {
            return target;
        }
        String prefix = defaultName() + "/";
        if (normalized.startsWith(prefix)) {
            return target + normalized.substring(prefix.length() - 1);
        }
        return normalized;
    }
}
