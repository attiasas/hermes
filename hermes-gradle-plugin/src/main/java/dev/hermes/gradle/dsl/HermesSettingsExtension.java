package dev.hermes.gradle.dsl;

import groovy.lang.Closure;

import java.io.File;

import org.gradle.api.GradleException;

/**
 * Engine and platform toggles configured in {@code settings.gradle}.
 */
public class HermesSettingsExtension {

    private File home;
    private String engineVersion;
    private String gameModule;
    private final SettingsPlatformsExtension platforms = new SettingsPlatformsExtension();

    public File getHome() {
        return home;
    }

    public void setHome(File home) {
        this.home = home;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public String getGameModule() {
        if (gameModule == null || gameModule.isBlank()) {
            throw new GradleException(
                    "hermes.gameModule is required in settings.gradle, e.g. hermes { gameModule = 'game' }");
        }
        return gameModule;
    }

    public void setGameModule(String gameModule) {
        this.gameModule = gameModule;
    }

    public SettingsPlatformsExtension getPlatforms() {
        return platforms;
    }

    @SuppressWarnings("rawtypes")
    public void platforms(Closure configure) {
        configure.setDelegate(platforms);
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(platforms);
    }
}
