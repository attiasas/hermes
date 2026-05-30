package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.api.scene.SceneDefinition;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoadMetadata;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.SceneRegistryImpl;
import dev.hermes.core.ecs.EntityStoreImpl;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.core.scene.AssetSceneSource;
import dev.hermes.core.scene.SceneStack;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ScenePipelineOverrideTest {

    private static final String PROJECT_DEFAULT = "render/full-pipeline.json";

    private ComponentRegistryImpl registry;
    private SceneRegistryImpl sceneRegistry;
    private SceneStack stack;

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        sceneRegistry = new SceneRegistryImpl(registry);
        stack = new SceneStack(sceneRegistry);
    }

    @Test
    void sceneLoader_readsRenderPipelineFromSceneJson() {
        EntityStoreImpl world = new EntityStoreImpl();
        SceneLoadMetadata metadata =
                SceneLoader.loadFromString(
                        "scenes/with-pipeline.json",
                        "{ \"renderPipeline\": \"render/ui-overlay.json\", \"entities\": [] }",
                        world,
                        registry);

        assertEquals(Optional.of("render/ui-overlay.json"), metadata.renderPipeline());
    }

    @Test
    void sceneStack_loadsRenderPipelineOverrideFromAssetScene() {
        sceneRegistry.register("overlay", "scenes/with-pipeline.json");
        stack.goTo("overlay");

        assertEquals(Optional.of("render/ui-overlay.json"), stack.active().renderPipelineOverride());
    }

    @Test
    void resolvePipelinePath_sceneJsonWinsOverDefinitionAndProjectDefault() {
        sceneRegistry.register(
                SceneDefinition.builder("pause")
                        .source(new AssetSceneSource("scenes/with-pipeline.json"))
                        .renderPipeline("render/full-pipeline.json")
                        .build());
        stack.goTo("pause");

        assertEquals(
                "render/ui-overlay.json",
                RenderPipelineExecutor.resolvePipelinePath(stack.active(), PROJECT_DEFAULT));
    }

    @Test
    void resolvePipelinePath_definitionUsedWhenNoJsonOverride() {
        sceneRegistry.register(
                SceneDefinition.builder("menu")
                        .source(new AssetSceneSource("scenes/main.json"))
                        .renderPipeline("render/ui-overlay.json")
                        .build());
        stack.goTo("menu");

        assertEquals(
                "render/ui-overlay.json",
                RenderPipelineExecutor.resolvePipelinePath(stack.active(), PROJECT_DEFAULT));
    }

    @Test
    void resolvePipelinePath_fallsBackToProjectDefault() {
        sceneRegistry.register("main", "scenes/main.json");
        stack.goTo("main");

        assertEquals(
                PROJECT_DEFAULT, RenderPipelineExecutor.resolvePipelinePath(stack.active(), PROJECT_DEFAULT));
    }

    @Test
    void pipelineCache_returnsSameGraphForSamePath() {
        PipelineCache cache = new PipelineCache();

        RenderGraph first = cache.get("render/ui-overlay.json");
        RenderGraph second = cache.get("render/ui-overlay.json");

        assertSame(first, second);
        assertEquals(1, first.passCount());
        assertEquals("ui", first.passId(0));

        cache.dispose();
    }

    @Test
    void resolvePipelinePath_nonSceneInstanceUsesOnlyJsonOverrideAndDefault() {
        SceneHandle handle =
                new SceneHandle() {
                    @Override
                    public String id() {
                        return "stub";
                    }

                    @Override
                    public dev.hermes.api.ecs.WorldManager manager() {
                        return null;
                    }

                    @Override
                    public boolean paused() {
                        return false;
                    }

                    @Override
                    public Optional<String> renderPipelineOverride() {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<String> inputContext() {
                        return Optional.empty();
                    }
                };

        assertEquals(
                PROJECT_DEFAULT, RenderPipelineExecutor.resolvePipelinePath(handle, PROJECT_DEFAULT));
    }

    @Test
    void assetSceneSource_populateStillLoadsEntities() {
        WorldManagerImpl manager = new WorldManagerImpl();
        AssetSceneSource source = new AssetSceneSource("scenes/with-pipeline.json");
        source.populate(
                new SceneLoadContext() {
                    @Override
                    public dev.hermes.api.ecs.WorldManager manager() {
                        return manager;
                    }

                    @Override
                    public dev.hermes.api.ecs.ComponentRegistry registry() {
                        return registry;
                    }
                });

        assertEquals(1, manager.entities().entityCount());
    }
}
