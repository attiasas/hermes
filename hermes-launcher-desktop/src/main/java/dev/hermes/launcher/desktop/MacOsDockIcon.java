package dev.hermes.launcher.desktop;

import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Sets the macOS Dock icon for the running JVM (bundle icon does not apply to the Java process).
 */
final class MacOsDockIcon {

    private static final String[] FAVICON_PATHS = {
            "assets/icons/web/favicon.png", "icons/web/favicon.png"
    };

    private MacOsDockIcon() {
    }

    static void install() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("mac")) {
            return;
        }
        if (!Taskbar.isTaskbarSupported()) {
            return;
        }
        Taskbar taskbar = Taskbar.getTaskbar();
        if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
            return;
        }
        try (InputStream in = openFaviconStream()) {
            if (in == null) {
                return;
            }
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                return;
            }
            taskbar.setIconImage(image);
        } catch (Exception ignored) {
            // Dock icon is optional; jlink runtime may omit modules needed for some image formats.
        }
    }

    private static InputStream openFaviconStream() {
        ClassLoader loader = MacOsDockIcon.class.getClassLoader();
        for (String path : FAVICON_PATHS) {
            InputStream in = loader.getResourceAsStream(path);
            if (in != null) {
                return in;
            }
        }
        return null;
    }
}
