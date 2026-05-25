package dev.hermes.tooling.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.hermes.tooling.config.HermesGameConfig;
import dev.hermes.tooling.platform.DesktopPlatform;
import dev.hermes.tooling.platform.HtmlPlatform;
import dev.hermes.tooling.platform.Platforms;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class LaunchConfigResolverTest {

    @Test
    void resolve_includesLoggingPatternsInDevMode() {
        HermesGameConfig game = new HermesGameConfig();
        game.setTitle("Test");
        game.setScene("scenes/a.json");
        game.setRenderPipeline("render/p.json");

        DesktopPlatform desktop = new DesktopPlatform();
        desktop.setWidth(800);
        desktop.setHeight(600);

        HtmlPlatform html = new HtmlPlatform();
        html.setDevServerPort(9090);

        Platforms platforms = new Platforms();
        platforms.getDesktop().copyDetailsFrom(desktop);
        platforms.getHtml().copyDetailsFrom(html);

        LaunchConfigRequest request =
                new LaunchConfigRequest(
                        "com.example.Game",
                        true,
                        LaunchMode.DEV,
                        "DEBUG",
                        "WILDCARD",
                        List.of("*SceneStack*"),
                        Map.of("custom.difficulty", "hard"),
                        game,
                        platforms);

        Map<String, String> props = LaunchConfigResolver.resolve(request).asMap();

        assertEquals("com.example.Game", props.get(RuntimeConfigKeys.APPLICATION_CLASS));
        assertEquals("true", props.get(RuntimeConfigKeys.DEBUG));
        assertEquals("DEBUG", props.get(RuntimeConfigKeys.LOG_MIN_LEVEL));
        assertEquals("WILDCARD", props.get(RuntimeConfigKeys.LOG_PATTERN_TYPE));
        assertEquals("*SceneStack*", props.get(RuntimeConfigKeys.LOG_PATTERNS));
        assertEquals("hard", props.get("hermes.custom.difficulty"));
        assertEquals("9090", props.get(RuntimeConfigKeys.HTML_DEV_SERVER_PORT));
    }

    @Test
    void resolve_exportModeForcesNonDebugAndWarnLevelWhenUnset() {
        HermesGameConfig game = new HermesGameConfig();
        Platforms platforms = new Platforms();

        LaunchConfigRequest request =
                new LaunchConfigRequest(
                        "com.example.Game",
                        true,
                        LaunchMode.DISTRIBUTION_EXPORT,
                        null,
                        null,
                        List.of(),
                        Map.of(),
                        game,
                        platforms);

        Map<String, String> props = LaunchConfigResolver.resolve(request).asMap();

        assertEquals("false", props.get(RuntimeConfigKeys.DEBUG));
        assertEquals("WARN", props.get(RuntimeConfigKeys.LOG_MIN_LEVEL));
        assertFalse(props.containsKey(RuntimeConfigKeys.LOG_PATTERNS));
    }
}
