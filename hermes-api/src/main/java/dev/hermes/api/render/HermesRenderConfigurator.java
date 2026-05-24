package dev.hermes.api.render;

/**
 * Registers custom render passes before the render pipeline is built.
 */
public final class HermesRenderConfigurator {

    private final RenderPassRegistry registry;

    public HermesRenderConfigurator(RenderPassRegistry registry) {
        this.registry = registry;
    }

    public void registerPass(String handler, RenderPass pass) {
        registry.register(handler, pass);
    }

    public RenderPassRegistry registry() {
        return registry;
    }
}
