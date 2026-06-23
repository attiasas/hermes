package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Drawables;
import dev.hermes.api.ecs.Transform;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class SpriteAliasLoadTest {

    private EntityStoreImpl world;
    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        world = new EntityStoreImpl();
        registry = new ComponentRegistryImpl();
        registry.setResources(ResourceManagerImpl.forTest("assets/resources/"));
        BuiltinComponents.register(registry);
    }

    @Test
    void loadsSpriteWithCatalogAliasTexture() {
        String json =
                "{\n"
                        + "  \"entities\": [\n"
                        + "    {\n"
                        + "      \"id\": \"logo\",\n"
                        + "      \"components\": {\n"
                        + "        \"Transform\": { \"x\": 0, \"y\": 0 },\n"
                        + "        \"Drawables\": { \"sprite\": \"@logo\" },\n"
                        + "        \"Material\": { \"shader\": \"default/unlit\" }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        SceneLoader.loadFromString("scenes/main.json", json, world, registry);

        Entity logo = world.findByName("logo");
        assertNotNull(logo);
        assertNotNull(world.getComponent(logo.id(), Transform.class));
        assertEquals("@logo", world.getComponent(logo.id(), Drawables.class).parts().get(0).texture());
    }

    @Test
    void loadsMeshWithCatalogAliasModel() {
        String json =
                "{\n"
                        + "  \"entities\": [\n"
                        + "    {\n"
                        + "      \"id\": \"hero\",\n"
                        + "      \"components\": {\n"
                        + "        \"Transform\": { \"x\": 0, \"y\": 0, \"z\": 0 },\n"
                        + "        \"Drawables\": { \"mesh\": \"@player-model\" },\n"
                        + "        \"Material\": { \"shader\": \"default/unlit\" }\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}\n";

        SceneLoader.loadFromString("scenes/world.json", json, world, registry);

        Entity hero = world.findByName("hero");
        assertNotNull(hero);
        assertEquals("@player-model", world.getComponent(hero.id(), Drawables.class).parts().get(0).model());
    }
}
