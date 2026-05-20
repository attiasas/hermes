package dev.hermes.launcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import dev.hermes.core.HermesGdxApplication;
import dev.hermes.core.HermesLauncherSupport;

/** Launches the desktop (LWJGL3) Hermes application. */
public final class Lwjgl3Launcher {

  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    boolean gradleRun = Boolean.getBoolean("hermes.desktop.gradleRun");
    // Gradle/IDE hermesRunDesktop passes -XstartOnFirstThread and hermes.desktop.gradleRun=true.
    // StartupHelper must not run there: spawning a child exits immediately or SIGTRAPs the parent.
    if (!gradleRun && StartupHelper.startNewJvmIfRequired()) {
      return;
    }
    // Dock icon is for exported/native bundles only (AWT before GLFW breaks Gradle dev runs).
    if (!gradleRun) {
      try {
        MacOsDockIcon.install();
      } catch (Throwable ignored) {
        // Dock icon must never prevent launch.
      }
    }
    createApplication();
  }

  private static void createApplication() {
    new Lwjgl3Application(
        new HermesGdxApplication(HermesLauncherSupport.loadApplication()), configuration());
  }

  private static Lwjgl3ApplicationConfiguration configuration() {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle(HermesLauncherSupport.windowTitle());
    configuration.useVsync(HermesLauncherSupport.desktopVsync());
    configuration.setResizable(HermesLauncherSupport.desktopResizable());
    int foregroundFps = HermesLauncherSupport.desktopForegroundFps();
    if (foregroundFps > 0) {
      configuration.setForegroundFPS(foregroundFps);
    } else {
      configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
    }
    configuration.setWindowedMode(HermesLauncherSupport.windowWidth(), HermesLauncherSupport.windowHeight());
    return configuration;
  }
}
