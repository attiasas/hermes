package dev.hermes.api.config;

/**
 * Typed read access to Hermes runtime configuration (packaged properties and startup overrides).
 */
public interface RuntimeConfigService {

    String get(String key, String defaultValue);

    boolean getBoolean(String key, boolean defaultValue);

    int getInt(String key, int defaultValue);

    boolean debug();

    String gameScene();

    String gameRenderPipeline();

    String gameInputProfile();

    String logMinLevel();

    int logMinSeverity();

    String logPatternType();

    String logPatterns();
}
