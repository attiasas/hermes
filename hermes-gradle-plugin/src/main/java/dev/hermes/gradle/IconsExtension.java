package dev.hermes.gradle;

import groovy.lang.Closure;

public final class IconsExtension {

  private String directory = "icons";
  private final IconsDesktopSpec desktop = new IconsDesktopSpec();
  private final IconsAndroidSpec android = new IconsAndroidSpec();
  private final IconsWebSpec web = new IconsWebSpec();

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public IconsDesktopSpec getDesktop() {
    return desktop;
  }

  public IconsAndroidSpec getAndroid() {
    return android;
  }

  public IconsWebSpec getWeb() {
    return web;
  }

  @SuppressWarnings("rawtypes")
  public void desktop(Closure configure) {
    configure.setDelegate(desktop);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(desktop);
  }

  @SuppressWarnings("rawtypes")
  public void android(Closure configure) {
    configure.setDelegate(android);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(android);
  }

  @SuppressWarnings("rawtypes")
  public void web(Closure configure) {
    configure.setDelegate(web);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(web);
  }
}
