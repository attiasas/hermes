package dev.hermes.tooling.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlatformModelDefaultsTest {

  @Test
  void desktopDefaults() {
    DesktopPlatform desktop = new DesktopPlatform();

    assertFalse(desktop.isEnabled());
    assertEquals(640, desktop.getWidth());
    assertEquals(480, desktop.getHeight());
    assertTrue(desktop.isVsync());
    assertTrue(desktop.isResizable());
    assertEquals(0, desktop.getForegroundFps());
    assertEquals(
        java.util.List.of("linuxX64", "macM1", "macX64", "winX64"), desktop.getExportTargets());
  }

  @Test
  void htmlDefaults() {
    HtmlPlatform html = new HtmlPlatform();

    assertFalse(html.isEnabled());
    assertEquals(640, html.getWidth());
    assertEquals(480, html.getHeight());
    assertEquals(8080, html.getDevServerPort());
    assertTrue(html.isWebAssembly());
  }

  @Test
  void androidDefaults() {
    AndroidPlatform android = new AndroidPlatform();

    assertFalse(android.isEnabled());
    assertEquals("dev.hermes.game", android.getApplicationId());
    assertEquals(21, android.getMinSdk());
    assertEquals(35, android.getTargetSdk());
    assertEquals(35, android.getCompileSdk());
    assertEquals(1, android.getVersionCode());
    assertEquals("landscape", android.getScreenOrientation());
  }

  @Test
  void mergeCopiesGameDetailsButKeepsSettingsEnabledFlags() {
    Platforms settings = new Platforms();
    settings.getDesktop().setEnabled(true);
    settings.getHtml().setEnabled(false);
    settings.getAndroid().setEnabled(true);

    Platforms game = new Platforms();
    game.getDesktop().setWidth(800);
    game.getDesktop().setHeight(600);
    game.getHtml().setDevServerPort(9000);
    game.getAndroid().setApplicationId("com.example.game");

    Platforms merged = Platforms.merge(settings, game);

    assertTrue(merged.getDesktop().isEnabled());
    assertEquals(800, merged.getDesktop().getWidth());
    assertEquals(600, merged.getDesktop().getHeight());
    assertFalse(merged.getHtml().isEnabled());
    assertEquals(9000, merged.getHtml().getDevServerPort());
    assertTrue(merged.getAndroid().isEnabled());
    assertEquals("com.example.game", merged.getAndroid().getApplicationId());
  }
}
