package dev.hermes.gradle;

import java.io.File;
import groovy.lang.Closure;

/** Engine and platform toggles configured in {@code settings.gradle}. */
public class HermesSettingsExtension {

  private File home;
  private String engineVersion;
  private final SettingsPlatformsExtension platforms = new SettingsPlatformsExtension();

  public File getHome() {
    return home;
  }

  public void setHome(File home) {
    this.home = home;
  }

  public String getEngineVersion() {
    return engineVersion;
  }

  public void setEngineVersion(String engineVersion) {
    this.engineVersion = engineVersion;
  }

  public SettingsPlatformsExtension getPlatforms() {
    return platforms;
  }

  @SuppressWarnings("rawtypes")
  public void platforms(Closure configure) {
    configure.setDelegate(platforms);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(platforms);
  }
}
