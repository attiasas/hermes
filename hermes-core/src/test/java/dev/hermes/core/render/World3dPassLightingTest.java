package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.lighting.BuiltinLightingSystem;
import dev.hermes.core.lighting.LightingRuntime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class World3dPassLightingTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        Gdx.graphics = new ResizableMockGraphics(800, 600);
    }

    @Test
    void lightingRuntime_reflectsSceneAmbientNotHardcodedDefault() {
        String json =
                "{\n"
                        + "  \"lighting\": {\n"
                        + "    \"version\": 1,\n"
                        + "    \"ambient\": { \"color\": [0.05, 0.05, 0.05, 1] }\n"
                        + "  },\n"
                        + "  \"entities\": []\n"
                        + "}\n";
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        WorldManagerImpl manager = new WorldManagerImpl();

        SceneLoader.loadFromString("dark.json", json, manager.entities(), registry);

        new BuiltinLightingSystem().update(manager, 0.016f);

        Environment env = LightingRuntime.require(manager.entities());
        ColorAttribute ambient = (ColorAttribute) env.get(ColorAttribute.AmbientLight);
        assertNotNull(ambient);
        assertEquals(0.05f, ambient.color.r, 0.001f);
        assertEquals(0.05f, ambient.color.g, 0.001f);
        assertEquals(0.05f, ambient.color.b, 0.001f);
        assertNotEquals(0.4f, ambient.color.r, 0.001f);
    }

    private static final class ResizableMockGraphics extends MockGraphics {
        private final int width;
        private final int height;

        ResizableMockGraphics(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int getBackBufferWidth() {
            return width;
        }

        @Override
        public int getBackBufferHeight() {
            return height;
        }
    }
}
