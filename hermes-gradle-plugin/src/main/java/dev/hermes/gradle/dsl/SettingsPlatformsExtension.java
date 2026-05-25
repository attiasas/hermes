package dev.hermes.gradle.dsl;

import dev.hermes.tooling.platform.AndroidPlatform;
import dev.hermes.tooling.platform.DesktopPlatform;
import dev.hermes.tooling.platform.HtmlPlatform;
import dev.hermes.tooling.platform.Platforms;
import groovy.lang.Closure;

/**
 * Platform enable flags and toggles configured in {@code settings.gradle}.
 */
public final class SettingsPlatformsExtension {

    private final Platforms platforms = new Platforms();

    public SettingsPlatformsExtension() {
        platforms.getDesktop().setEnabled(true);
    }

    public DesktopPlatform getDesktop() {
        return platforms.getDesktop();
    }

    public HtmlPlatform getHtml() {
        return platforms.getHtml();
    }

    public AndroidPlatform getAndroid() {
        return platforms.getAndroid();
    }

    Platforms asPlatforms() {
        return platforms;
    }

    @SuppressWarnings("rawtypes")
    public void desktop(Closure configure) {
        configure.setDelegate(getDesktop());
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(getDesktop());
    }

    @SuppressWarnings("rawtypes")
    public void html(Closure configure) {
        configure.setDelegate(getHtml());
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(getHtml());
    }

    @SuppressWarnings("rawtypes")
    public void android(Closure configure) {
        configure.setDelegate(getAndroid());
        configure.setResolveStrategy(Closure.DELEGATE_FIRST);
        configure.call(getAndroid());
    }
}
