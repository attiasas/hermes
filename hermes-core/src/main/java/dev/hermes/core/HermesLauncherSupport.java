package dev.hermes.core;

import dev.hermes.api.HermesApplication;
import dev.hermes.core.config.RuntimeConfigServices;

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
        return RuntimeConfigServices.get().debug();
    }

    public static int windowWidth() {
        return RuntimeConfigServices.get().getInt("hermes.window.width", 640);
    }

    public static int windowHeight() {
        return RuntimeConfigServices.get().getInt("hermes.window.height", 480);
    }

    public static String windowTitle() {
        return RuntimeConfigServices.get().get("hermes.window.title", "Hermes");
    }

    public static boolean desktopVsync() {
        return RuntimeConfigServices.get().getBoolean("hermes.desktop.vsync", true);
    }

    public static boolean desktopResizable() {
        return RuntimeConfigServices.get().getBoolean("hermes.desktop.resizable", true);
    }

    public static int desktopForegroundFps() {
        return RuntimeConfigServices.get().getInt("hermes.desktop.foregroundFps", 0);
    }

    public static String gameScenePath() {
        return RuntimeConfigServices.get().gameScene();
    }

    public static String gameRenderPipelinePath() {
        return RuntimeConfigServices.get().gameRenderPipeline();
    }
}
