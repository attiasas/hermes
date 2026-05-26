package dev.hermes.tooling.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GameModuleNamesTest {

    @Test
    void normalize_defaultGame() {
        assertEquals("game", GameModuleNames.normalize(null));
        assertEquals("game", GameModuleNames.normalize(""));
        assertEquals("game", GameModuleNames.normalize("  "));
    }

    @Test
    void normalize_acceptsHyphens() {
        assertEquals("my-game", GameModuleNames.normalize("my-game"));
    }

    @Test
    void normalize_rejectsLauncherPrefix() {
        assertThrows(
                IllegalArgumentException.class,
                () -> GameModuleNames.normalize("hermes-launcher-desktop"));
    }

    @Test
    void normalize_rejectsInvalidChars() {
        assertThrows(IllegalArgumentException.class, () -> GameModuleNames.normalize("my.game"));
    }

    @Test
    void parseFromSettingsGradle_readsGameModule() {
        String content =
                "hermes {\n"
                        + "    gameModule = 'my-game'\n"
                        + "}\n"
                        + "include 'my-game'\n";
        assertEquals("my-game", GameModuleNames.parseFromSettingsGradle(content));
    }

    @Test
    void parseFromSettingsGradle_defaultsWhenMissing() {
        assertEquals("game", GameModuleNames.parseFromSettingsGradle("include 'game'"));
    }
}
