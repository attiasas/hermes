package dev.hermes.gradle.dsl;

public final class IconsDesktopSpec {

    private String mac = "icons/desktop/mac.icns";
    private String windows = "icons/desktop/windows.png";

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getWindows() {
        return windows;
    }

    public void setWindows(String windows) {
        this.windows = windows;
    }
}
