package dev.hermes.gradle;

import groovy.lang.Closure;

/** Platform enable flags only — configured in {@code settings.gradle}. */
public final class SettingsPlatformsExtension {

  private final PlatformEnableSpec desktop = new PlatformEnableSpec();
  private final PlatformEnableSpec html = new PlatformEnableSpec();
  private final PlatformEnableSpec android = new PlatformEnableSpec();

  public SettingsPlatformsExtension() {
    desktop.setEnabled(true);
  }

  public PlatformEnableSpec getDesktop() {
    return desktop;
  }

  public PlatformEnableSpec getHtml() {
    return html;
  }

  public PlatformEnableSpec getAndroid() {
    return android;
  }

  @SuppressWarnings("rawtypes")
  public void desktop(Closure configure) {
    configure.setDelegate(desktop);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(desktop);
  }

  @SuppressWarnings("rawtypes")
  public void html(Closure configure) {
    configure.setDelegate(html);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(html);
  }

  @SuppressWarnings("rawtypes")
  public void android(Closure configure) {
    configure.setDelegate(android);
    configure.setResolveStrategy(Closure.DELEGATE_FIRST);
    configure.call(android);
  }
}
