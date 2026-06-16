package dev.hermes.tooling.launch;

import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.platform.DesktopPlatform;
import dev.hermes.tooling.platform.HtmlPlatform;
import dev.hermes.tooling.platform.Platforms;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LaunchConfigResolver {

    private LaunchConfigResolver() {}

    public static HermesLaunchProperties resolve(LaunchConfigRequest request) {
        boolean export = request.mode() == LaunchMode.DISTRIBUTION_EXPORT;
        boolean debug = !export && request.debugFlag();
        String minLevel = resolveMinLevel(request.loggingMinLevel(), debug, export);
        String patternType = normalizePatternType(request.loggingPatternType());

        HermesGameConfig game = request.gameConfig();
        DesktopPlatform desktop = request.platforms().getDesktop();
        HtmlPlatform html = request.platforms().getHtml();

        HermesLaunchProperties.Builder builder =
                HermesLaunchProperties.builder()
                        .applicationClass(request.applicationClass())
                        .debug(debug)
                        .windowTitle(game.getTitle())
                        .windowSize(desktop.getWidth(), desktop.getHeight())
                        .scene(game.getScene())
                        .renderPipeline(game.getRenderPipeline())
                        .inputProfile(game.getInputProfile())
                        .audioProfile(game.getAudioProfile())
                        .resourceProfile(game.getResourceProfile())
                        .loadingScreen(game.getLoadingScreen())
                        .desktopVsync(desktop.isVsync())
                        .desktopResizable(desktop.isResizable())
                        .desktopForegroundFps(desktop.getForegroundFps())
                        .htmlDevServerPort(html.getDevServerPort())
                        .htmlWebAssembly(html.isWebAssembly())
                        .logMinLevel(minLevel);

        if (request.loggingPatterns() != null && !request.loggingPatterns().isEmpty()) {
            builder.logPatternType(patternType);
            builder.logPatterns(request.loggingPatterns());
        }

        if (request.customProperties() != null) {
            for (Map.Entry<String, String> entry : request.customProperties().entrySet()) {
                builder.custom(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    private static String resolveMinLevel(String explicit, boolean debug, boolean export) {
        if (explicit != null && !explicit.isBlank()) {
            return explicit.trim().toUpperCase(Locale.ROOT);
        }
        if (export) {
            return "WARN";
        }
        return debug ? "DEBUG" : "INFO";
    }

    private static String normalizePatternType(String patternType) {
        if (patternType == null || patternType.isBlank()) {
            return "WILDCARD";
        }
        return patternType.trim().toUpperCase(Locale.ROOT);
    }
}
