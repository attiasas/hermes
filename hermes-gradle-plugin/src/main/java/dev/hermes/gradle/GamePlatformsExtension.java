package dev.hermes.gradle;

import groovy.lang.Closure;

/** Platform details configured in {@code game/build.gradle}. */
public final class GamePlatformsExtension {

  private final GameDesktopPlatformSpec desktop = new GameDesktopPlatformSpec();
  private final GameHtmlPlatformSpec html = new GameHtmlPlatformSpec();
  private final GameAndroidPlatformSpec android = new GameAndroidPlatformSpec();

  public GameDesktopPlatformSpec getDesktop() {
    return desktop;
  }

  public GameHtmlPlatformSpec getHtml() {
    return html;
  }

  public GameAndroidPlatformSpec getAndroid() {
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
