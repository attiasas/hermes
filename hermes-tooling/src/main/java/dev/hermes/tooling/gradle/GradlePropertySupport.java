package dev.hermes.tooling.gradle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Reads Gradle project properties from {@code gradle.properties} on disk.
 */
public final class GradlePropertySupport {

    private GradlePropertySupport() {
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public static String readProperty(Path projectDir, String key) {
        Path props = projectDir.resolve("gradle.properties");
        if (!Files.isRegularFile(props)) {
            return null;
        }
        Properties properties = new Properties();
        try (var in = Files.newInputStream(props)) {
            properties.load(in);
            return properties.getProperty(key);
        } catch (IOException e) {
            return null;
        }
    }
}
