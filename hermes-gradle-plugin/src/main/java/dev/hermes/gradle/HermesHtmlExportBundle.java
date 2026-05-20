package dev.hermes.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.gradle.api.GradleException;

/** Adds run instructions for HTML exports (WebAssembly cannot load over file://). */
final class HermesHtmlExportBundle {

  private static final String README =
      """
      Hermes HTML export
      ==================

      Do not open webapp/index.html directly in the browser (file://). Browsers block
      loading app.wasm from file URLs (CORS / security).

      Serve the webapp folder over HTTP, then open the URL shown:

        macOS / Linux:  ./serve.sh
        Windows:        serve.bat

      Or manually:

        cd webapp
        python3 -m http.server 8080

      Then visit http://127.0.0.1:8080/ in your browser.

      During development, use: ./gradlew :game:hermesRunHtml
      """;

  private static final String SERVE_SH =
      """
      #!/usr/bin/env sh
      cd "$(dirname "$0")/webapp" || exit 1
      port="${1:-8080}"
      echo "Serving Hermes HTML export at http://127.0.0.1:${port}/"
      echo "(file:// cannot load WebAssembly — use this HTTP URL)"
      if command -v python3 >/dev/null 2>&1; then
        exec python3 -m http.server "$port"
      fi
      if command -v python >/dev/null 2>&1; then
        exec python -m http.server "$port"
      fi
      echo "Install Python 3 or use: npx --yes serve -l $port" >&2
      exit 1
      """;

  private static final String SERVE_BAT =
      """
      @echo off
      cd /d "%~dp0webapp"
      set PORT=%1
      if "%PORT%"=="" set PORT=8080
      echo Serving Hermes HTML export at http://127.0.0.1:%PORT%/
      echo (file:// cannot load WebAssembly — use this HTTP URL)
      py -m http.server %PORT% 2>nul && exit /b 0
      python -m http.server %PORT% 2>nul && exit /b 0
      echo Install Python 3 or use: npx --yes serve -l %PORT%
      exit /b 1
      """;

  private HermesHtmlExportBundle() {}

  static void writeInto(File exportRoot) {
    try {
      Files.writeString(new File(exportRoot, "README.txt").toPath(), README, StandardCharsets.UTF_8);
      File serveSh = new File(exportRoot, "serve.sh");
      Files.writeString(serveSh.toPath(), SERVE_SH, StandardCharsets.UTF_8);
      serveSh.setExecutable(true, false);
      Files.writeString(new File(exportRoot, "serve.bat").toPath(), SERVE_BAT, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new GradleException("Failed to write HTML export instructions under " + exportRoot, e);
    }
  }
}
