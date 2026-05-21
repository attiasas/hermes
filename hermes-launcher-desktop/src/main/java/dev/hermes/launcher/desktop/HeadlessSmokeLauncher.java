package dev.hermes.launcher.desktop;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import dev.hermes.core.HermesGdxApplication;
import dev.hermes.core.HermesLauncherSupport;

/** Headless desktop launch for CI and scripts; exits after {@code hermes.desktop.smokeFrames} renders. */
public final class HeadlessSmokeLauncher {

  private HeadlessSmokeLauncher() {}

  public static void main(String[] args) {
    int smokeFrames = readSmokeFrames();
    if (smokeFrames <= 0) {
      throw new IllegalStateException(
          "HeadlessSmokeLauncher requires -Dhermes.desktop.smokeFrames > 0 "
              + "(set via -Phermes.desktop.smokeFrames on hermesRunDesktop)");
    }
    HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
    configuration.updatesPerSecond = 60;
    new HeadlessApplication(
        new HermesGdxApplication(HermesLauncherSupport.loadApplication()), configuration);
  }

  private static int readSmokeFrames() {
    String value = System.getProperty("hermes.desktop.smokeFrames");
    if (value == null || value.isBlank()) {
      return 0;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
