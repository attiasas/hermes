package dev.hermes.launcher.html;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;

import java.io.File;
import java.io.IOException;

import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

/**
 * Builds the TeaVM/HTML application.
 */
public final class TeaVMBuilder {

    private static final String RUNTIME_CONFIG_DIR = "hermes.runtime.config.dir";

    private TeaVMBuilder() {
    }

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
        int devServerPort = Integer.parseInt(System.getProperty("hermes.html.devServerPort", "8080"));
        boolean webAssembly = Boolean.parseBoolean(System.getProperty("hermes.html.webAssembly", "true"));

        String assetsPath = System.getProperty("hermes.assets.dir");
        if (assetsPath == null || assetsPath.isBlank()) {
            throw new IllegalStateException(
                    "hermes.assets.dir system property is required (set by :game:hermesRunHtml or HTML launcher tasks).");
        }

        String runtimeConfigDirPath = System.getProperty(RUNTIME_CONFIG_DIR);
        if (runtimeConfigDirPath == null || runtimeConfigDirPath.isBlank()) {
            throw new IllegalStateException(
                    RUNTIME_CONFIG_DIR + " is required (set by :game:hermesRunHtml).");
        }
        File runtimeConfigDir = new File(runtimeConfigDirPath);
        File runtimeConfigFile = new File(runtimeConfigDir, "hermes-runtime.properties");
        if (!runtimeConfigFile.isFile()) {
            throw new IllegalStateException(
                    "Missing canonical runtime config: " + runtimeConfigFile.getAbsolutePath()
                            + " (run :game:generateHermesRuntimeConfig first).");
        }

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
}
