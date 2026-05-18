package dev.hermes.gradle;

/** Android platform details on {@code :game}. */
public final class GameAndroidPlatformSpec {

  private String applicationId = "dev.hermes.game";
  private int minSdk = 21;
  private int targetSdk = 35;
  private int compileSdk = 35;
  private int versionCode = 1;
  private String screenOrientation = "landscape";

  public String getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  public int getMinSdk() {
    return minSdk;
  }

  public void setMinSdk(int minSdk) {
    this.minSdk = minSdk;
  }

  public int getTargetSdk() {
    return targetSdk;
  }

  public void setTargetSdk(int targetSdk) {
    this.targetSdk = targetSdk;
  }

  public int getCompileSdk() {
    return compileSdk;
  }

  public void setCompileSdk(int compileSdk) {
    this.compileSdk = compileSdk;
  }

  public int getVersionCode() {
    return versionCode;
  }

  public void setVersionCode(int versionCode) {
    this.versionCode = versionCode;
  }

  public String getScreenOrientation() {
    return screenOrientation;
  }

  public void setScreenOrientation(String screenOrientation) {
    this.screenOrientation = screenOrientation;
  }
}
