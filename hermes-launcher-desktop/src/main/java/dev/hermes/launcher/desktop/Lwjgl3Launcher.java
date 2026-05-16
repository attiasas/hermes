package dev.hermes.launcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import dev.hermes.core.HermesGdxApplication;
import dev.hermes.sample.SampleHermesGame;

/** Launches the desktop (LWJGL3) Hermes application. */
public final class Lwjgl3Launcher {

  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired()) {
      return;
    }
    createApplication();
  }

  private static Lwjgl3Application createApplication() {
    return new Lwjgl3Application(new HermesGdxApplication(new SampleHermesGame()), defaultConfiguration());
  }

  private static Lwjgl3ApplicationConfiguration defaultConfiguration() {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("Hermes (sample)");
    configuration.useVsync(true);
    configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
    configuration.setWindowedMode(640, 480);
    return configuration;
  }
}
