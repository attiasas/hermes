package dev.hermes.gradle.dsl;

import groovy.lang.Closure;

/**
 * Project-level Hermes configuration on {@code :game}.
 */
public class HermesExtension {

    private String applicationClass;
    private String assetsDirectory;
    private boolean debug;
    private final PlatformsExtension platforms = new PlatformsExtension();
    private final IconsExtension icons = new IconsExtension();
    private final LoggingExtension logging = new LoggingExtension();

    public String getApplicationClass() {
        return applicationClass;
    }

    public void setApplicationClass(String applicationClass) {
        this.applicationClass = applicationClass;
    }

    public String getAssetsDirectory() {
        return assetsDirectory;
    }

    public void setAssetsDirectory(String assetsDirectory) {
        this.assetsDirectory = assetsDirectory;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public PlatformsExtension getPlatforms() {
        return platforms;
    }

    public LoggingExtension getLogging() {
        return logging;
    }

    @SuppressWarnings("rawtypes")
    public void logging(Closure configure) {
        configure.setDelegate(logging);
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(logging);
    }

    @SuppressWarnings("rawtypes")
    public void platforms(Closure configure) {
        configure.setDelegate(platforms);
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(platforms);
    }

    public IconsExtension getIcons() {
        return icons;
    }

    @SuppressWarnings("rawtypes")
    public void icons(Closure configure) {
        configure.setDelegate(icons);
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(icons);
    }
}
