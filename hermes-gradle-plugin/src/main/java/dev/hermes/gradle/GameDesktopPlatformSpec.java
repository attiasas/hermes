package dev.hermes.gradle;

import java.util.Arrays;
import java.util.List;

/** Desktop platform details on {@code :game} (no {@code enabled} — that lives in settings). */
public final class GameDesktopPlatformSpec {

  private int width = 640;
  private int height = 480;
  private boolean vsync = true;
  private boolean resizable = true;
  private int foregroundFps = 0;
  private String bundleId;
  private String executableName;
  private List<String> exportTargets =
      Arrays.asList("linuxX64", "macM1", "macX64", "winX64");

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public boolean isVsync() {
    return vsync;
  }

  public void setVsync(boolean vsync) {
    this.vsync = vsync;
  }

  public boolean isResizable() {
    return resizable;
  }

  public void setResizable(boolean resizable) {
    this.resizable = resizable;
  }

  public int getForegroundFps() {
    return foregroundFps;
  }

  public void setForegroundFps(int foregroundFps) {
    this.foregroundFps = foregroundFps;
  }

  public String getBundleId() {
    return bundleId;
  }

  public void setBundleId(String bundleId) {
    this.bundleId = bundleId;
  }

  public String getExecutableName() {
    return executableName;
  }

  public void setExecutableName(String executableName) {
    this.executableName = executableName;
  }

  public List<String> getExportTargets() {
    return exportTargets;
  }

  public void setExportTargets(List<String> exportTargets) {
    this.exportTargets = exportTargets;
  }
}
