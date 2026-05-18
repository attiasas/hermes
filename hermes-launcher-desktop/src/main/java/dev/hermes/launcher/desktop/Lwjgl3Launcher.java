package dev.hermes.launcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import dev.hermes.core.HermesGdxApplication;
import dev.hermes.core.HermesLauncherSupport;

/** Launches the desktop (LWJGL3) Hermes application. */
public final class Lwjgl3Launcher {

  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired()) {
      return;
    }
    try {
      MacOsDockIcon.install();
    } catch (Throwable ignored) {
      // Dock icon must never prevent launch.
    }
    createApplication();
  }

  private static Lwjgl3Application createApplication() {
    return new Lwjgl3Application(
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
