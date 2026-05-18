package dev.hermes.gradle;

public final class HtmlPlatformSpec extends PlatformEnableSpec {

  private int width = 640;
  private int height = 480;
  private int devServerPort = 8080;
  private boolean webAssembly = true;

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

  public int getDevServerPort() {
    return devServerPort;
  }

  public void setDevServerPort(int devServerPort) {
    this.devServerPort = devServerPort;
  }

  public boolean isWebAssembly() {
    return webAssembly;
  }

  public void setWebAssembly(boolean webAssembly) {
    this.webAssembly = webAssembly;
  }
}
