package dev.hermes.gradle;

import groovy.lang.Closure;

/** Platform toggles configured in {@code settings.gradle}. */
public class HermesSettingsExtension {

  private final PlatformsExtension platforms = new PlatformsExtension();

  public PlatformsExtension getPlatforms() {
    return platforms;
  }

  @SuppressWarnings("rawtypes")
  public void platforms(Closure configure) {
    configure.setDelegate(platforms);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(platforms);
  }
}
