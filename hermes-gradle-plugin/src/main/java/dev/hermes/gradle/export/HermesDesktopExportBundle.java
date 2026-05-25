package dev.hermes.gradle.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.gradle.api.GradleException;

/**
 * Adds run instructions for desktop exports (macOS Gatekeeper, unzip permissions).
 */
final class HermesDesktopExportBundle {

    private static final String README =
            """
                    Hermes desktop export
                    =====================
                    
                    macOS
                    -----
                    1. Unzip this archive.
                    2. Open the .app bundle inside (not the ZIP itself).
                    
                    If macOS says the app "can't be opened":
                      - Right-click the .app → Open → Open (first launch for unsigned apps), or
                      - Terminal: xattr -cr "/path/to/YourGame.app"
                      - If needed: chmod +x "/path/to/YourGame.app/Contents/MacOS/"*
                    
                    If double-click does nothing (no window, no error):
                      - Run from Terminal to see crash output:
                        "/path/to/YourGame.app/Contents/MacOS/YourGame"
                      - Re-export after updating the Hermes Gradle plugin (older builds could not
                        find scenes packaged under assets/).
                    
                    Linux
                    -----
                    Unzip, then run the launcher binary in the bundle (or chmod +x if needed).
                    
                    Windows
                    -------
                    Unzip and run the .exe inside the folder.
                    """;

    private HermesDesktopExportBundle() {
    }

    static void writeInto(File exportRoot) {
        try {
            Files.writeString(new File(exportRoot, "README.txt").toPath(), README, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GradleException("Failed to write desktop export README under " + exportRoot, e);
        }
    }
}
