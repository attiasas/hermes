package dev.hermes.gradle;

import groovy.lang.Closure;

public class PlatformsExtension {

  private final DesktopPlatformSpec desktop = new DesktopPlatformSpec();
  private final HtmlPlatformSpec html = new HtmlPlatformSpec();
  private final AndroidPlatformSpec android = new AndroidPlatformSpec();

  public PlatformsExtension() {
    this(true);
  }

  PlatformsExtension(boolean defaultDesktopEnabled) {
    if (defaultDesktopEnabled) {
      desktop.setEnabled(true);
    }
  }

  public DesktopPlatformSpec getDesktop() {
    return desktop;
  }

  public HtmlPlatformSpec getHtml() {
    return html;
  }

  public AndroidPlatformSpec getAndroid() {
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
