package dev.hermes.gradle;

import java.io.File;
import groovy.lang.Closure;

/** Project-level Hermes configuration on {@code :game}. */
public class HermesExtension {

  private String applicationClass;
  private String assetsDirectory;
  private String engineVersion;
  private File home;
  private boolean debug = false;
  private final PlatformsExtension platforms = new PlatformsExtension();

  public String getEngineVersion() {
    return engineVersion;
  }

  public void setEngineVersion(String engineVersion) {
    this.engineVersion = engineVersion;
  }

  public File getHome() {
    return home;
  }

  public void setHome(File home) {
    this.home = home;
  }

  public HermesExtension() {
    platforms.getDesktop().setEnabled(true);
  }

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
