package dev.hermes.tooling.platform;

/**
 * Android platform configuration.
 */
public final class AndroidPlatform {

    private boolean enabled;
    private String applicationId = "dev.hermes.game";
    private int minSdk = 21;
    private int targetSdk = 35;
    private int compileSdk = 35;
    private int versionCode = 1;
    private String screenOrientation = "landscape";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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

    /**
     * Copies all fields except {@code enabled} from {@code source}.
     */
    public void copyDetailsFrom(AndroidPlatform source) {
        setApplicationId(source.getApplicationId());
        setMinSdk(source.getMinSdk());
        setTargetSdk(source.getTargetSdk());
        setCompileSdk(source.getCompileSdk());
        setVersionCode(source.getVersionCode());
        setScreenOrientation(source.getScreenOrientation());
    }
}
