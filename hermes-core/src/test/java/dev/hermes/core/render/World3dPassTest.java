package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.Entity;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.SceneParseException;
import dev.hermes.core.ecs.WorldImpl;
import dev.hermes.core.render.pass.World3dPass;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class World3dPassTest {

    private WorldImpl world;
    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        world = new WorldImpl();
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void collectDrawables_returnsMeshEntitiesWithMaterial() {
        FileHandle scene = Gdx.files.internal("assets/scenes/mesh-cube.json");
        SceneLoader.loadFromString(
                "assets/scenes/mesh-cube.json",
                scene.readString(StandardCharsets.UTF_8.name()),
                world,
                registry);

        List<Entity> drawables = World3dPass.collectDrawables(world);
        assertEquals(1, drawables.size());
        assertEquals("cube", drawables.get(0).name());
    }

    @Test
    void meshWithoutMaterial_failsSceneLoad() {
        String json =
                "{\n"
                        + "  \"entities\": [{\n"
                        + "    \"id\": \"bad\",\n"
                        + "    \"components\": {\n"
                        + "      \"Transform\": {},\n"
                        + "      \"Mesh\": { \"model\": \"models/cube.obj\" }\n"
                        + "    }\n"
                        + "  }]\n"
                        + "}\n";

        SceneParseException error =
                assertThrows(
                        SceneParseException.class,
                        () -> SceneLoader.loadFromString("scenes/bad.json", json, world, registry));

        assertTrue(error.getMessage().contains("Mesh requires a Material"));
    }
}
