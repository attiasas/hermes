package dev.hermes.tooling.platform;

/**
 * Holder for desktop, HTML, and Android platform configuration.
 */
public final class Platforms {

    private final DesktopPlatform desktop = new DesktopPlatform();
    private final HtmlPlatform html = new HtmlPlatform();
    private final AndroidPlatform android = new AndroidPlatform();

    public DesktopPlatform getDesktop() {
        return desktop;
    }

    public HtmlPlatform getHtml() {
        return html;
    }

    public AndroidPlatform getAndroid() {
        return android;
    }

    /**
     * Merges settings enable flags with game DSL details.
     *
     * @param settings platforms from {@code settings.gradle} (typically only {@code enabled} is set)
     * @param game     platforms from {@code :game} build.gradle
     */
    public static Platforms merge(Platforms settings, Platforms game) {
        Platforms merged = new Platforms();
        merged.getDesktop().setEnabled(settings.getDesktop().isEnabled());
        merged.getDesktop().copyDetailsFrom(game.getDesktop());
        merged.getHtml().setEnabled(settings.getHtml().isEnabled());
        merged.getHtml().copyDetailsFrom(game.getHtml());
        merged.getAndroid().setEnabled(settings.getAndroid().isEnabled());
        merged.getAndroid().copyDetailsFrom(game.getAndroid());
        return merged;
    }
}
