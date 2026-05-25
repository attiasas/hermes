package dev.hermes.gradle.platform;

import dev.hermes.tooling.HermesEngineVersions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Token values for rendering synced platform {@code build.gradle} files.
 */
public record PlatformSyncContext(
        String moduleName, String engineVersion, String agpVersion) {

    public static PlatformSyncContext forStandalone(
            String moduleName, String engineVersion, File rootDir) {
        return new PlatformSyncContext(
                moduleName, engineVersion, resolveAndroidGradlePluginVersion(rootDir));
    }

    public Map<String, String> templateTokens() {
        Map<String, String> tokens = new HashMap<>();
        tokens.put("{{engineVersion}}", engineVersion);
        tokens.put("{{hermesCoreDependency}}", hermesCoreDependency());
        tokens.put("{{gameDependency}}", gameDependency());
        tokens.put("{{agpVersion}}", agpVersion);
        return tokens;
    }

    public String hermesCoreDependency() {
        return "implementation 'dev.hermes:hermes-core:" + engineVersion + "'";
    }

    public String gameDependency() {
        if ("hermes-launcher-html".equals(moduleName)) {
            return "compileOnly project(':game')";
        }
        return "implementation project(':game')";
    }

    private static String resolveAndroidGradlePluginVersion(File rootDir) {
        File propsFile = new File(rootDir, "gradle.properties");
        if (propsFile.isFile()) {
            try {
                Properties props = new Properties();
                try (InputStream in = Files.newInputStream(propsFile.toPath())) {
                    props.load(in);
                }
                return HermesEngineVersions.resolveAndroidGradlePluginVersion(props);
            } catch (IOException ignored) {
                // fall through
            }
        }
        return HermesEngineVersions.defaultAndroidGradlePluginVersion();
    }
}
