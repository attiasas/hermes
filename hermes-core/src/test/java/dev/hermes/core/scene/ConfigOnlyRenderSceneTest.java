package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.EntityStoreImpl;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Config-only scene loading for 3D mesh entities (no GPU).
 */
final class ConfigOnlyRenderSceneTest {

    private EntityStoreImpl world;
    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        world = new EntityStoreImpl();
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
    }

    @Test
    void meshCubeScene_loadsMeshAndMaterialFromJsonOnly() {
        FileHandle scene = Gdx.files.internal("assets/scenes/mesh-cube.json");
        SceneLoader.loadFromString(
                "assets/scenes/mesh-cube.json",
                scene.readString(StandardCharsets.UTF_8.name()),
                world,
                registry);

        var cube = world.findByName("cube");
        assertNotNull(cube);
        Drawables drawables = world.getComponent(cube.id(), Drawables.class);
        assertNotNull(drawables);
        assertEquals("models/cube.obj", drawables.parts().get(0).model());
    }
}
