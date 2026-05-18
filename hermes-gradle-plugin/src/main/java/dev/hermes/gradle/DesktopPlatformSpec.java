package dev.hermes.gradle;

public final class DesktopPlatformSpec extends PlatformEnableSpec {

  private int width = 640;
  private int height = 480;
  private boolean vsync = true;
  private boolean resizable = true;
  /** 0 = match display refresh rate + 1 (libGDX default behavior). */
  private int foregroundFps = 0;
  private String bundleId;
  private String executableName;
  private java.util.List<String> exportTargets =
      java.util.Arrays.asList("linuxX64", "macM1", "macX64", "winX64");

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

  public java.util.List<String> getExportTargets() {
    return exportTargets;
  }

  public void setExportTargets(java.util.List<String> exportTargets) {
    this.exportTargets = exportTargets;
  }
}
