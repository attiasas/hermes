package dev.hermes.gradle;

import groovy.lang.Closure;

public class PlatformsExtension {

  private final PlatformSpec desktop = new PlatformSpec();
  private final PlatformSpec html = new PlatformSpec();
  private final PlatformSpec android = new PlatformSpec();

  public PlatformsExtension() {
    desktop.setEnabled(true);
    html.setEnabled(false);
    android.setEnabled(false);
  }

  public PlatformSpec getDesktop() {
    return desktop;
  }

  public PlatformSpec getHtml() {
    return html;
  }

  public PlatformSpec getAndroid() {
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
