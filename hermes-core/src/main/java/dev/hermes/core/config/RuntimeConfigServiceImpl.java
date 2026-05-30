package dev.hermes.core.config;

import dev.hermes.api.config.RuntimeConfigBuilder;
import dev.hermes.api.config.RuntimeConfigService;
import dev.hermes.api.log.LogLevel;
import dev.hermes.core.HermesRuntimeConfig;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class RuntimeConfigServiceImpl implements RuntimeConfigService, RuntimeConfigBuilder {

    private final Map<String, String> overrides = new LinkedHashMap<>();

    @Override
    public void put(String key, String value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        String normalized = key.startsWith("hermes.")
                ? key
                : key.startsWith("custom.") ? "hermes." + key : "hermes.custom." + key;
        overrides.put(normalized, value);
    }

    public void applyOverrides() {
        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
    }

  public RuntimeConfigBuilder builder() {
        return this;
    }

    @Override
    public String get(String key, String defaultValue) {
        String fromOverride = overrides.get(key);
        if (fromOverride != null) {
            return fromOverride;
        }
        return HermesRuntimeConfig.get(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, Boolean.toString(defaultValue)));
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = get(key, "");
        if (value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    @Override
    public boolean debug() {
        return getBoolean("hermes.debug", false);
    }

    @Override
    public String gameScene() {
        return get("hermes.game.scene", "scenes/main.json");
    }

    @Override
    public String gameRenderPipeline() {
        return get("hermes.game.renderPipeline", "render/pipeline.json");
    }

    @Override
    public String gameInputProfile() {
        return get("hermes.game.inputProfile", "input/profile.json");
    }

    @Override
    public String gameAudioProfile() {
        return get("hermes.game.audioProfile", "audio/profile.json");
    }

    @Override
    public String logMinLevel() {
        String explicit = get("hermes.log.minLevel", "");
        if (!explicit.isBlank()) {
            return explicit.trim().toUpperCase(Locale.ROOT);
        }
        return debug() ? "DEBUG" : "INFO";
    }

    @Override
    public int logMinSeverity() {
        return LogLevel.parse(logMinLevel()).severity();
    }

    @Override
    public String logPatternType() {
        String type = get("hermes.log.patternType", "WILDCARD");
        return type.isBlank() ? "WILDCARD" : type.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public String logPatterns() {
        return get("hermes.log.patterns", "");
    }
}
