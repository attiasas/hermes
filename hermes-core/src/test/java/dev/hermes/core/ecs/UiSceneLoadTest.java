package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.UiAttach;
import dev.hermes.core.TestGdx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class UiSceneLoadTest {

    private WorldManagerImpl manager;
    private ComponentRegistryImpl components;
    private EntityTypeRegistryImpl types;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        types = new EntityTypeRegistryImpl();
        components = new ComponentRegistryImpl();
        BuiltinComponents.register(components);
        manager = new WorldManagerImpl(types, components);
    }

    @Test
    void sceneLoaderParsesUiFieldAndUiAttach() {
        SceneLoadMetadata metadata =
                SceneLoader.load(
                        "scenes/ui-scene-test.json", manager.entities(), components, types);

        assertEquals("ui/test-panel.json", metadata.uiConfig().orElseThrow().document());
        assertEquals("fit", metadata.uiConfig().orElseThrow().fitMode());

        Entity hp = manager.entities().findByName("hp");
        assertNotNull(hp);
        assertTrue(manager.entities().hasComponent(hp.id(), UiAttach.class));

        UiAttach attach = manager.entities().getComponent(hp.id(), UiAttach.class);
        assertEquals("ui/hp-bar.json", attach.document());
        assertEquals("player", attach.follow());
        assertEquals(2.0f, attach.offsetY(), 0.001f);
    }
}
