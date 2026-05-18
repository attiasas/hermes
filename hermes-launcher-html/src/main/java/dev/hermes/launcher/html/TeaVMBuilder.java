package dev.hermes.launcher.html;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
public final class TeaVMBuilder {

  private TeaVMBuilder() {}

  public static void main(String[] args) throws IOException {
    boolean debug = Boolean.parseBoolean(System.getProperty("hermes.debug", "false"));
    boolean startJetty = false;
    for (String arg : args) {
      if ("debug".equals(arg)) {
        debug = true;
      } else if ("run".equals(arg)) {
        startJetty = true;
      }
    }

    String applicationClass = System.getProperty("hermes.applicationClass");
    if (applicationClass == null || applicationClass.isBlank()) {
      throw new IllegalStateException(
          "hermes.applicationClass system property is required (set by :game:hermesRunHtml).");
    }

    String title = System.getProperty("hermes.window.title", "Hermes");
    int width = Integer.parseInt(System.getProperty("hermes.window.width", "640"));
    int height = Integer.parseInt(System.getProperty("hermes.window.height", "480"));
    String gameTitle = System.getProperty("hermes.game.title", "HermesGame");
    String gameScene = System.getProperty("hermes.game.scene", "scenes/main.json");
    int devServerPort = Integer.parseInt(System.getProperty("hermes.html.devServerPort", "8080"));
    boolean webAssembly = Boolean.parseBoolean(System.getProperty("hermes.html.webAssembly", "true"));
    String assetsPath = System.getProperty("hermes.assets.dir");
    if (assetsPath == null || assetsPath.isBlank()) {
      throw new IllegalStateException(
          "hermes.assets.dir system property is required (set by :game:hermesRunHtml or HTML launcher tasks).");
    }

    File runtimeConfigDir = new File("build/hermes-runtime");
    writeRuntimeProperties(
        runtimeConfigDir, applicationClass, debug, title, width, height, gameTitle, gameScene);

    File launcherSources = new File("src/main/java");
    TeaCompiler compiler =
        new TeaCompiler(
                new WebBackend()
                    .setHtmlWidth(width)
                    .setHtmlHeight(height)
                    .setHtmlTitle(title)
                    .setWebAssembly(webAssembly)
                    .setStartJettyAfterBuild(startJetty)
                    .setJettyPort(devServerPort))
            .addAssets(new AssetFileHandle(assetsPath))
            .addAssets(new AssetFileHandle(runtimeConfigDir.getPath()))
            .setOptimizationLevel(debug ? TeaVMOptimizationLevel.SIMPLE : TeaVMOptimizationLevel.ADVANCED)
            .setMainClass("dev.hermes.launcher.html.generated.GeneratedTeaVMLauncher")
            .setObfuscated(!debug)
            .setDebugInformationGenerated(debug)
            .setSourceMapsFileGenerated(debug)
            .setSourceFilePolicy(TeaVMSourceFilePolicy.COPY)
            .addSourceFileProvider(new DirectorySourceFileProvider(launcherSources));

    addSourcesIfPresent(compiler, sourcesDir("hermes.game.sources.dir", "../../game/src/main/java", "../game/src/main/java"));
    addSourcesIfPresent(compiler, sourcesDir("hermes.engine.core.sources.dir", "../../hermes-core/src/main/java", "../hermes-core/src/main/java"));
    addSourcesIfPresent(compiler, sourcesDir("hermes.engine.api.sources.dir", "../../hermes-api/src/main/java", "../hermes-api/src/main/java"));

    compiler.build(new File("build/dist"));
  }

  private static File sourcesDir(String property, String... candidates) {
    String fromProperty = System.getProperty(property);
    if (fromProperty != null && !fromProperty.isBlank()) {
      return new File(fromProperty);
    }
    for (String candidate : candidates) {
      File dir = new File(candidate);
      if (dir.isDirectory()) {
        return dir;
      }
    }
    return new File(candidates[0]);
  }

  private static void addSourcesIfPresent(TeaCompiler compiler, File dir) {
    if (dir.isDirectory()) {
      compiler.addSourceFileProvider(new DirectorySourceFileProvider(dir));
    }
  }

  private static void writeRuntimeProperties(
      File dir,
      String applicationClass,
      boolean debug,
      String title,
      int width,
      int height,
      String gameTitle,
      String gameScene)
      throws IOException {
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IOException("Could not create " + dir.getAbsolutePath());
    }
    Properties properties = new Properties();
    properties.setProperty("hermes.applicationClass", applicationClass);
    properties.setProperty("hermes.debug", Boolean.toString(debug));
    properties.setProperty("hermes.window.title", title);
    properties.setProperty("hermes.window.width", Integer.toString(width));
    properties.setProperty("hermes.window.height", Integer.toString(height));
    properties.setProperty("hermes.game.title", gameTitle);
    properties.setProperty("hermes.game.scene", gameScene);
    File file = new File(dir, "hermes-runtime.properties");
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
      properties.store(writer, "Generated by TeaVMBuilder for HTML/WASM runtime");
    }
  }
}
