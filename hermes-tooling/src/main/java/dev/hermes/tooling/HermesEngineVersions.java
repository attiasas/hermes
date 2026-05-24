package dev.hermes.tooling;

import dev.hermes.tooling.gradle.HermesHomeResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * libGDX / launcher version keys for generated game projects.
 */
public final class HermesEngineVersions {

    public static final String[] GRADLE_PROPERTY_KEYS = {
            "gdxVersion",
            "lwjgl3Version",
            "gdxTeaVMVersion",
            "android.useAndroidX",
            "hermes.androidGradlePluginVersion",
            "construoVersion",
    };

    /**
     * Keys written into generated game {@code gradle.properties} (libGDX versions are injected by the settings plugin).
     */
    public static final String[] NEW_PROJECT_GRADLE_PROPERTY_KEYS = {
            "android.useAndroidX",
            "hermes.androidGradlePluginVersion",
    };

    private static final String DEFAULT_GDX_VERSION = "1.14.0";
    private static final String DEFAULT_LWJGL3_VERSION = "3.4.1";
    private static final String DEFAULT_GDX_TEAVM_VERSION = "1.5.5";
    private static final String DEFAULT_ANDROID_USE_ANDROIDX = "true";
    public static final String ANDROID_GRADLE_PLUGIN_VERSION_KEY = "hermes.androidGradlePluginVersion";
    private static final String DEFAULT_ANDROID_GRADLE_PLUGIN_VERSION = "8.9.3";
    private static final String DEFAULT_CONSTRUO_VERSION = "2.1.0";

    private HermesEngineVersions() {
    }

    public static String defaultAndroidGradlePluginVersion() {
        return DEFAULT_ANDROID_GRADLE_PLUGIN_VERSION;
    }

    public static String resolveAndroidGradlePluginVersion(Properties gradleProperties) {
        if (gradleProperties != null) {
            String version = gradleProperties.getProperty(ANDROID_GRADLE_PLUGIN_VERSION_KEY);
            if (version != null && !version.isBlank()) {
                return version.trim();
            }
        }
        return defaultAndroidGradlePluginVersion();
    }

    public static Properties defaults() {
        Properties props = new Properties();
        props.setProperty("gdxVersion", DEFAULT_GDX_VERSION);
        props.setProperty("lwjgl3Version", DEFAULT_LWJGL3_VERSION);
        props.setProperty("gdxTeaVMVersion", DEFAULT_GDX_TEAVM_VERSION);
        props.setProperty("android.useAndroidX", DEFAULT_ANDROID_USE_ANDROIDX);
        props.setProperty("hermes.androidGradlePluginVersion", DEFAULT_ANDROID_GRADLE_PLUGIN_VERSION);
        props.setProperty("construoVersion", DEFAULT_CONSTRUO_VERSION);
        return props;
    }

    public static Properties loadFromEngineHome(File hermesHome) {
        if (hermesHome == null || !HermesHomeResolver.isHermesCheckout(hermesHome)) {
            return null;
        }
        Path propsFile = hermesHome.toPath().resolve("gradle.properties");
        if (!Files.isRegularFile(propsFile)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(propsFile)) {
            props.load(in);
            return props;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Hermes-specific keys for {@code hermes new} (not libGDX version pins).
     */
    public static Properties resolveForNewProject(File hermesHome) {
        Properties resolved = new Properties();
        Properties defaults = defaults();
        for (String key : NEW_PROJECT_GRADLE_PROPERTY_KEYS) {
            resolved.setProperty(key, defaults.getProperty(key));
        }
        Properties fromEngine = loadFromEngineHome(hermesHome);
        if (fromEngine == null) {
            return resolved;
        }
        for (String key : NEW_PROJECT_GRADLE_PROPERTY_KEYS) {
            String value = fromEngine.getProperty(key);
            if (value != null && !value.isBlank()) {
                resolved.setProperty(key, value);
            }
        }
        return resolved;
    }
}
