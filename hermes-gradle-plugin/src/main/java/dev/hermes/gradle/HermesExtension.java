package dev.hermes.gradle;

import groovy.lang.Closure;

/** Project-level Hermes configuration on {@code :game}. */
public class HermesExtension {

  private String applicationClass;
  private boolean debug = false;
  private final PlatformsExtension platforms = new PlatformsExtension();

  public HermesExtension() {
    platforms.getDesktop().setEnabled(true);
  }

  public String getApplicationClass() {
    return applicationClass;
  }

  public void setApplicationClass(String applicationClass) {
    this.applicationClass = applicationClass;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

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
