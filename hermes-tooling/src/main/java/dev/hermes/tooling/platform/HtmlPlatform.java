package dev.hermes.tooling.platform;

/**
 * HTML (TeaVM) platform configuration.
 */
public final class HtmlPlatform {

    private boolean enabled;
    private int width = 640;
    private int height = 480;
    private int devServerPort = 8080;
    private boolean webAssembly = true;

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

    /**
     * Copies all fields except {@code enabled} from {@code source}.
     */
    public void copyDetailsFrom(HtmlPlatform source) {
        setWidth(source.getWidth());
        setHeight(source.getHeight());
        setDevServerPort(source.getDevServerPort());
        setWebAssembly(source.isWebAssembly());
    }
}
