package dev.hermes.core.render;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.render.RenderContext;
import dev.hermes.api.render.RenderPass;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.core.TestGdx;
import dev.hermes.core.ecs.BuiltinComponents;
import dev.hermes.core.ecs.ComponentRegistryImpl;
import dev.hermes.core.ecs.SceneLoader;
import dev.hermes.core.ecs.WorldManagerImpl;
import dev.hermes.api.scene.SceneLoadContext;
import dev.hermes.core.resource.ResourceManagerImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class AdvancedRenderPipelineTest {

    @BeforeAll
    static void init() {
        TestGdx.initHeadlessGl();
        TestGdx.initClasspathFiles();
    }

    @Test
    void advancedRenderPipeline_rendersWithoutThrowing() {
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        WorldManagerImpl manager = new WorldManagerImpl();
        SceneLoader.load(
                "demos/advanced-render.json",
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

        RenderPassRegistry passes = new RenderPassRegistry();
        passes.register(
                "water",
                new RenderPass() {
                    @Override
                    public void resize(int width, int height) {}

                    @Override
                    public void render(EntityStore entities, RenderContext context) {}

                    @Override
                    public void dispose() {}
                });
        PipelineCache cache =
                new PipelineCache(
                        null,
                        passes,
                        new ViewportServiceImpl(),
                        null,
                        ResourceManagerImpl.createDefault());
        cache.resize(640, 480);
        RenderGraph graph = cache.get("render/advanced-render-pipeline.json");

        assertDoesNotThrow(() -> graph.render(manager.entities()));
        graph.dispose();
        cache.dispose();
    }
}
