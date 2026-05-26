package dev.hermes.gradle.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HermesDependencyResolverLauncherTest {

    @Test
    void htmlLauncherUsesCompileOnlyGame() {
        assertTrue(HermesDependencyResolver.isCompileOnlyGameDependency("hermes-launcher-html"));
        assertFalse(HermesDependencyResolver.isCompileOnlyGameDependency("hermes-launcher-desktop"));
    }

    @Test
    void mavenCoordinateFormat() {
        assertTrue(HermesDependencyResolver.mavenCoordinate("hermes-core", "1.0.0")
                .contains("dev.hermes:hermes-core:1.0.0"));
    }

    @Test
    void gameProjectPath_usesConfiguredModuleName() {
        assertEquals(":my-game-module", HermesDependencyResolver.gameProjectPath("my-game-module"));
        assertEquals(":game", HermesDependencyResolver.gameProjectPath("game"));
    }
}
