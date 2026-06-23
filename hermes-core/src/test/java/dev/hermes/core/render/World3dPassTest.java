package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.DrawablePart;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.SceneParseException;
import dev.hermes.core.ecs.EntityStoreImpl;
import dev.hermes.core.render.pass.World3dPass;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class World3dPassTest {

    private EntityStoreImpl world;
    private ComponentRegistryImpl registry;
    private ResourceManagerImpl resources;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        world = new EntityStoreImpl();
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        resources = ResourceManagerImpl.createDefault();
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
    void collectDrawables_multiPartEntity_returnsOneDrawable() {
        Entity entity = world.create("multi");
        world.addComponent(entity.id(), new Transform());
        world.addComponent(entity.id(), new Material());
        world.addComponent(
                entity.id(),
                new Drawables(
                        List.of(
                                DrawablePart.mesh("body", "models/cube.obj"),
                                DrawablePart.mesh("hat", "models/cube.obj"))));

        List<Entity> drawables = World3dPass.collectDrawables(world);
        assertEquals(1, drawables.size());
        assertEquals("multi", drawables.get(0).name());
    }

    @Test
    void meshWithoutMaterial_failsSceneLoad() {
        String json =
                "{\n"
                        + "  \"entities\": [{\n"
                        + "    \"id\": \"bad\",\n"
                        + "    \"components\": {\n"
                        + "      \"Transform\": {},\n"
                        + "      \"Drawables\": { \"mesh\": \"models/cube.obj\" }\n"
                        + "    }\n"
                        + "  }]\n"
                        + "}\n";

        SceneParseException error =
                assertThrows(
                        SceneParseException.class,
                        () -> SceneLoader.loadFromString("scenes/bad.json", json, world, registry));

        assertTrue(error.getMessage().contains("Drawables requires a Material"));
    }

    @Test
    void loadSync_resolvesModelThroughResourceAccess() {
        ResourceRef ref = ResourceRef.of("models/cube.obj");
        resources.loadSync(ref, ResourceKind.MODEL);
        assertTrue(resources.isLoaded(ref, ResourceKind.MODEL));
        Model model = ResourceAccess.model(resources, ref);
        assertNotNull(model);
    }
}
