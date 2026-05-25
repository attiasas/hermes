package dev.hermes.tooling.platform;

import java.util.Arrays;
import java.util.List;

/**
 * Desktop (LWJGL3) platform configuration.
 */
public final class DesktopPlatform {

    private boolean enabled;
    private int width = 640;
    private int height = 480;
    private boolean vsync = true;
    private boolean resizable = true;
    /**
     * 0 = match display refresh rate + 1 (libGDX default behavior).
     */
    private int foregroundFps;
    private String bundleId;
    private String executableName;
    private List<String> exportTargets =
            Arrays.asList("linuxX64", "macM1", "macX64", "winX64");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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

    /**
     * Copies all fields except {@code enabled} from {@code source}.
     */
    public void copyDetailsFrom(DesktopPlatform source) {
        setWidth(source.getWidth());
        setHeight(source.getHeight());
        setVsync(source.isVsync());
        setResizable(source.isResizable());
        setForegroundFps(source.getForegroundFps());
        setBundleId(source.getBundleId());
        setExecutableName(source.getExecutableName());
        setExportTargets(source.getExportTargets());
    }
}
