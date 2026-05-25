package dev.hermes.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Resolves Hermes launch settings from JVM system properties or {@code hermes-runtime.properties} on the classpath.
 */
public final class HermesRuntimeConfig {

    private static final Properties PACKAGED = loadPackaged();

    private HermesRuntimeConfig() {
    }

    public static String get(String key, String defaultValue) {
        String fromJvm = System.getProperty(key);
        if (fromJvm != null && !fromJvm.isBlank()) {
            return fromJvm;
        }
        return PACKAGED.getProperty(key, defaultValue);
    }

    public static void reload() {
        // stub for Task 1; Task 2 replaces with real lazy load
    }

    private static Properties loadPackaged() {
        Properties properties = new Properties();
        try (InputStream in =
                     HermesRuntimeConfig.class.getClassLoader().getResourceAsStream("hermes-runtime.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load hermes-runtime.properties", e);
        }
        return properties;
    }
}
