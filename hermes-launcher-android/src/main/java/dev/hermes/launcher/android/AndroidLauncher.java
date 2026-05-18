package dev.hermes.launcher.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import dev.hermes.core.HermesGdxApplication;
import dev.hermes.core.HermesLauncherSupport;

/** Launches the Android Hermes application. */
public final class AndroidLauncher extends AndroidApplication {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (System.getProperty("hermes.applicationClass") == null) {
      System.setProperty("hermes.applicationClass", BuildConfig.HERMES_APPLICATION_CLASS);
    }
    super.onCreate(savedInstanceState);
    AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
    configuration.useImmersiveMode = true;
    initialize(new HermesGdxApplication(HermesLauncherSupport.loadApplication()), configuration);
  }
}
