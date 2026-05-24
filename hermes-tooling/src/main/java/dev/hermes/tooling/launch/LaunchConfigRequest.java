package dev.hermes.tooling.launch;

import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.platform.Platforms;
import java.util.List;
import java.util.Map;

public final class LaunchConfigRequest {

    private final String applicationClass;
    private final boolean debugFlag;
    private final LaunchMode mode;
    private final String loggingMinLevel;
    private final String loggingPatternType;
    private final List<String> loggingPatterns;
    private final Map<String, String> customProperties;
    private final HermesGameConfig gameConfig;
    private final Platforms platforms;

    public LaunchConfigRequest(
            String applicationClass,
            boolean debugFlag,
            LaunchMode mode,
            String loggingMinLevel,
            String loggingPatternType,
            List<String> loggingPatterns,
            Map<String, String> customProperties,
            HermesGameConfig gameConfig,
            Platforms platforms) {
        this.applicationClass = applicationClass;
        this.debugFlag = debugFlag;
        this.mode = mode;
        this.loggingMinLevel = loggingMinLevel;
        this.loggingPatternType = loggingPatternType;
        this.loggingPatterns = loggingPatterns;
        this.customProperties = customProperties;
        this.gameConfig = gameConfig;
        this.platforms = platforms;
    }

    public String applicationClass() {
        return applicationClass;
    }

    public boolean debugFlag() {
        return debugFlag;
    }

    public LaunchMode mode() {
        return mode;
    }

    public String loggingMinLevel() {
        return loggingMinLevel;
    }

    public String loggingPatternType() {
        return loggingPatternType;
    }

    public List<String> loggingPatterns() {
        return loggingPatterns;
    }

    public Map<String, String> customProperties() {
        return customProperties;
    }

    public HermesGameConfig gameConfig() {
        return gameConfig;
    }

    public Platforms platforms() {
        return platforms;
    }
}
