package dev.hermes.gradle;

import groovy.lang.Closure;

/** Project-level Hermes configuration on {@code :game}. */
public class HermesExtension {

  private String applicationClass;
  private String assetsDirectory;
  private boolean debug = false;
  private final GamePlatformsExtension platforms = new GamePlatformsExtension();
  private final IconsExtension icons = new IconsExtension();

  public String getApplicationClass() {
    return applicationClass;
  }

  public void setApplicationClass(String applicationClass) {
    this.applicationClass = applicationClass;
  }

  public String getAssetsDirectory() {
    return assetsDirectory;
  }

  public void setAssetsDirectory(String assetsDirectory) {
    this.assetsDirectory = assetsDirectory;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public GamePlatformsExtension getPlatforms() {
    return platforms;
  }

  @SuppressWarnings("rawtypes")
  public void platforms(Closure configure) {
    configure.setDelegate(platforms);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(platforms);
  }

  public IconsExtension getIcons() {
    return icons;
  }

  @SuppressWarnings("rawtypes")
  public void icons(Closure configure) {
    configure.setDelegate(icons);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(icons);
  }
}
