package dev.hermes.core;

import dev.hermes.api.HermesApplication;

/**
 * Loads the user {@link HermesApplication} from launch configuration (JVM properties or packaged properties).
 */
public final class HermesLauncherSupport {

    private HermesLauncherSupport() {
    }

    public static HermesApplication loadApplication() {
        String className = HermesRuntimeConfig.get("hermes.applicationClass", "");
        if (className.isBlank()) {
            throw new IllegalStateException(
                    "hermes.applicationClass is required (set by the Hermes Gradle plugin or hermes-runtime.properties).");
        }
        try {
            Class<?> type = Class.forName(className);
            Object instance = type.getDeclaredConstructor().newInstance();
            if (!(instance instanceof HermesApplication)) {
                throw new IllegalStateException(className + " does not implement HermesApplication");
            }
            return (HermesApplication) instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to load Hermes application: " + className, e);
        }
    }

    public static boolean isDebugEnabled() {
        return Boolean.parseBoolean(HermesRuntimeConfig.get("hermes.debug", "false"));
    }

    public static int windowWidth() {
        return Integer.parseInt(HermesRuntimeConfig.get("hermes.window.width", "640"));
    }

    public static int windowHeight() {
        return Integer.parseInt(HermesRuntimeConfig.get("hermes.window.height", "480"));
    }

    public static String windowTitle() {
        return HermesRuntimeConfig.get("hermes.window.title", "Hermes");
    }

    public static boolean desktopVsync() {
        return Boolean.parseBoolean(HermesRuntimeConfig.get("hermes.desktop.vsync", "true"));
    }

    public static boolean desktopResizable() {
        return Boolean.parseBoolean(HermesRuntimeConfig.get("hermes.desktop.resizable", "true"));
    }

    public static int desktopForegroundFps() {
        return Integer.parseInt(HermesRuntimeConfig.get("hermes.desktop.foregroundFps", "0"));
    }

    public static String gameScenePath() {
        return HermesRuntimeConfig.get("hermes.game.scene", "scenes/main.json");
    }

    public static String gameRenderPipelinePath() {
        return HermesRuntimeConfig.get("hermes.game.renderPipeline", "render/pipeline.json");
    }
}
