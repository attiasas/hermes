package dev.hermes.core.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.core.HermesAssetPaths;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class InputProfileLoaderTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void load_parsesProfileWithBindingsFromTestResource() {
        InputProfile profile = InputProfileLoader.load("input/profile.json");

        assertEquals(1, profile.version());
        assertEquals("gameplay", profile.defaultContext());
        assertEquals(4, profile.actions().size());
        assertEquals(InputProfile.ActionType.AXIS, profile.actions().get("move_x"));
        assertEquals(InputProfile.ActionType.BUTTON, profile.actions().get("select"));
        assertTrue(profile.bindings().size() >= 5, "expected at least 5 bindings");
        assertEquals(7, profile.bindings().size());
        assertEquals(0.15f, profile.gamepadDeadzone(), 0.0001f);
    }

    @Test
    void load_findsResourceViaHermesAssetPaths() {
        assertTrue(HermesAssetPaths.internal("input/profile.json").exists());
        InputProfile profile = InputProfileLoader.load("input/profile.json");
        assertEquals("pause", profile.bindings().get(5).action());
    }
}
