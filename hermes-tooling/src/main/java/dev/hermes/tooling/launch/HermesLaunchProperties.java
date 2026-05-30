package dev.hermes.tooling.launch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Builder for JVM system properties used by Hermes desktop/HTML run and export.
 */
public final class HermesLaunchProperties {

    private final Map<String, String> properties;

    private HermesLaunchProperties(Map<String, String> properties) {
        this.properties = Map.copyOf(properties);
    }

    public Map<String, String> asMap() {
        return properties;
    }

    public void applyTo(Properties target) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            target.setProperty(entry.getKey(), entry.getValue());
        }
    }

    public List<String> toJvmArgs() {
        List<String> args = new ArrayList<>(properties.size());
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            args.add("-D" + entry.getKey() + "=" + entry.getValue());
        }
        return args;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, String> properties = new LinkedHashMap<>();

        public Builder applicationClass(String value) {
            return put("hermes.applicationClass", value);
        }

        public Builder debug(boolean value) {
            return put("hermes.debug", Boolean.toString(value));
        }

        public Builder windowTitle(String value) {
            return put("hermes.window.title", value);
        }

        public Builder windowSize(int width, int height) {
            put("hermes.window.width", Integer.toString(width));
            put("hermes.window.height", Integer.toString(height));
            return this;
        }

        public Builder scene(String value) {
            return put("hermes.game.scene", value);
        }

        public Builder renderPipeline(String value) {
            return put(RuntimeConfigKeys.GAME_RENDER_PIPELINE, value);
        }

        public Builder inputProfile(String value) {
            return put(RuntimeConfigKeys.GAME_INPUT_PROFILE, value);
        }

        public Builder audioProfile(String value) {
            return put(RuntimeConfigKeys.GAME_AUDIO_PROFILE, value);
        }

        public Builder desktopVsync(boolean value) {
            return put("hermes.desktop.vsync", Boolean.toString(value));
        }

        public Builder desktopResizable(boolean value) {
            return put("hermes.desktop.resizable", Boolean.toString(value));
        }

        public Builder desktopForegroundFps(int value) {
            return put("hermes.desktop.foregroundFps", Integer.toString(value));
        }

        public Builder desktopGradleRun() {
            return put("hermes.desktop.gradleRun", "true");
        }

        public Builder htmlDevServerPort(int port) {
            return put("hermes.html.devServerPort", Integer.toString(port));
        }

        public Builder htmlWebAssembly(boolean value) {
            return put("hermes.html.webAssembly", Boolean.toString(value));
        }

        public Builder assetsDir(String absolutePath) {
            return put("hermes.assets.dir", absolutePath);
        }

        public Builder gameSourcesDir(String absolutePath) {
            return put("hermes.game.sources.dir", absolutePath);
        }

        private Builder put(String key, String value) {
            if (value != null) {
                properties.put(key, value);
            }
            return this;
        }

        public Builder logMinLevel(String value) {
            return put("hermes.log.minLevel", value);
        }

        public Builder logPatternType(String value) {
            return put("hermes.log.patternType", value);
        }

        public Builder logPatterns(List<String> values) {
            if (values != null && !values.isEmpty()) {
                return put("hermes.log.patterns", String.join(";", values));
            }
            return this;
        }

        public Builder custom(String key, String value) {
            if (key == null || key.isBlank()) {
                return this;
            }
            if (key.startsWith("hermes.")) {
                return put(key, value);
            }
            if (key.startsWith("custom.")) {
                return put("hermes." + key, value);
            }
            return put("hermes.custom." + key, value);
        }

        public Builder runtimeConfigDir(String absolutePath) {
            return put(RuntimeConfigKeys.RUNTIME_CONFIG_DIR, absolutePath);
        }

        public Builder putAll(Map<String, String> values) {
            if (values != null) {
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public HermesLaunchProperties build() {
            return new HermesLaunchProperties(properties);
        }
    }
}
