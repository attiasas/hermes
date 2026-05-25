package dev.hermes.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HermesRuntimeConfigTest {

    @BeforeAll
    static void initGdx() {
        if (Gdx.files == null) {
            Gdx.files = new ClasspathFiles();
        }
    }

    @BeforeEach
    void clearJvmOverrides() {
        System.clearProperty("hermes.log.minLevel");
        System.clearProperty("hermes.log.patterns");
        System.clearProperty("hermes.debug");
        HermesRuntimeConfig.reload();
    }

    @AfterEach
    void cleanup() {
        System.clearProperty("hermes.log.minLevel");
        HermesRuntimeConfig.reload();
    }

    @Test
    void get_loadsFromInternalAssetsWhenClasspathMissing() {
        assertEquals("WARN", HermesRuntimeConfig.get("hermes.log.minLevel", "INFO"));
        assertEquals("*SceneStack*", HermesRuntimeConfig.get("hermes.log.patterns", ""));
        assertEquals("false", HermesRuntimeConfig.get("hermes.debug", "true"));
    }

    @Test
    void get_jvmOverrideWinsOverPackaged() {
        System.setProperty("hermes.log.minLevel", "ERROR");
        HermesRuntimeConfig.reload();
        assertEquals("ERROR", HermesRuntimeConfig.get("hermes.log.minLevel", "INFO"));
    }
}
