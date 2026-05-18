package dev.hermes.gradle;

/** Common {@code enabled} flag for platform blocks in {@code settings.gradle}. */
public class PlatformEnableSpec {

  private boolean enabled = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
