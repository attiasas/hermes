package dev.hermes.core.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.ecs.RenderLayer;
import dev.hermes.api.render.RenderPassRegistry;
import dev.hermes.core.render.pass.SpritesPass;
import dev.hermes.core.render.pass.UiRenderPass;
import dev.hermes.core.render.pass.World3dPass;
import dev.hermes.core.render.resource.ModelCache;
import dev.hermes.core.render.resource.ShaderRegistry;
import dev.hermes.core.ui.UiServiceImpl;
import dev.hermes.core.viewport.ViewportServiceImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds a {@link RenderGraph} from a parsed {@link PipelineDocument}.
 */
public final class RenderGraphBuilder {

    private final ModelCache sharedModelCache = new ModelCache();
    private final RenderPassRegistry passRegistry;
    private final ViewportServiceImpl viewport;
    private final UiServiceImpl ui;

    public RenderGraphBuilder() {
        this(new RenderPassRegistry(), new ViewportServiceImpl(), null);
    }

    public RenderGraphBuilder(RenderPassRegistry passRegistry) {
        this(passRegistry, new ViewportServiceImpl(), null);
    }

    RenderGraphBuilder(RenderPassRegistry passRegistry, ViewportServiceImpl viewport) {
        this(passRegistry, viewport, null);
    }

    RenderGraphBuilder(RenderPassRegistry passRegistry, ViewportServiceImpl viewport, UiServiceImpl ui) {
        this.passRegistry = passRegistry == null ? new RenderPassRegistry() : passRegistry;
        this.viewport = viewport == null ? new ViewportServiceImpl() : viewport;
        this.ui = ui;
    }

    public RenderGraph build(PipelineDocument document, SpriteBatch batch) {
        ShaderRegistry shaderRegistry = new ShaderRegistry(document.shaders());
        FramebufferPool pool = new FramebufferPool(document.framebuffers());
        validatePassTargets(document);
        validateCustomHandlers(document);
        List<RenderGraphPass> passes = createPasses(document, batch, shaderRegistry, pool);
        return new RenderGraph(
                document.clearColor(), passes, sharedModelCache, shaderRegistry, pool, viewport);
    }

    /**
     * Builds a graph with no-op passes for unit tests (same order and validation as {@link #build}).
     */
    RenderGraph buildWithStubs(PipelineDocument document) {
        FramebufferPool pool = new FramebufferPool(document.framebuffers(), false);
        validatePassTargets(document);
        validateCustomHandlers(document);
        List<RenderGraphPass> passes = createStubPasses(document, pool);
        return new RenderGraph(document.clearColor(), passes, null, null, pool, viewport);
    }

    private List<RenderGraphPass> createPasses(
            PipelineDocument document,
            SpriteBatch batch,
            ShaderRegistry shaderRegistry,
            FramebufferPool pool) {
        List<RenderGraphPass> passes = new ArrayList<>();
        for (PipelineDocument.PassDef passDef : document.passes()) {
            Set<RenderLayer.Layer> layers = parseLayers(passDef.layers());
            RenderGraphPass pass = createPass(passDef, batch, layers, shaderRegistry);
            passes.add(wrapTarget(passDef, pass, pool));
        }
        return passes;
    }

    private List<RenderGraphPass> createStubPasses(
            PipelineDocument document, FramebufferPool pool) {
        List<RenderGraphPass> passes = new ArrayList<>();
        for (PipelineDocument.PassDef passDef : document.passes()) {
            parseLayers(passDef.layers());
            RenderGraphPass pass;
            if (passDef.type() == PipelineDocument.PassType.CUSTOM) {
                pass =
                        new CustomGraphPass(
                                passDef.id(), passRegistry.require(passDef.handler()), viewport);
            } else if (passDef.type() == PipelineDocument.PassType.UI) {
                pass = new UiGraphPass(passDef.id(), new UiRenderPass(), passDef.depthTest());
            } else if (passDef.type() == PipelineDocument.PassType.POST
                    || passDef.type() == PipelineDocument.PassType.PARTICLES
                    || passDef.type() == PipelineDocument.PassType.COMPUTE) {
                pass = new UnimplementedGraphPass(passDef.id(), passDef.type());
            } else {
                pass = new StubRenderGraphPass(passDef.id());
            }
            passes.add(wrapTarget(passDef, pass, pool));
        }
        return passes;
    }

    private TargetBindingGraphPass wrapTarget(
            PipelineDocument.PassDef passDef, RenderGraphPass pass, FramebufferPool pool) {
        return new TargetBindingGraphPass(
                passDef.target(), passDef.camera(), pass, pool, viewport);
    }

    private static void validatePassTargets(PipelineDocument document) {
        Set<String> framebufferIds = new HashSet<>();
        for (PipelineDocument.FramebufferDef def : document.framebuffers()) {
            framebufferIds.add(def.id());
        }
        for (PipelineDocument.PassDef pass : document.passes()) {
            String target = pass.target();
            if (!"screen".equals(target) && !framebufferIds.contains(target)) {
                throw new PipelineParseException(
                        "pass \""
                                + pass.id()
                                + "\" targets unknown framebuffer: "
                                + target
                                + " (declare it in \"framebuffers\")");
            }
        }
    }

    private RenderGraphPass createPass(
            PipelineDocument.PassDef passDef,
            SpriteBatch batch,
            Set<RenderLayer.Layer> layers,
            ShaderRegistry shaderRegistry) {
        switch (passDef.type()) {
            case WORLD3D:
                return new World3dGraphPass(
                        passDef.id(),
                        new World3dPass(sharedModelCache, shaderRegistry, false),
                        layers,
                        passDef.depthTest());
            case SPRITES:
                return new SpritesGraphPass(
                        passDef.id(), new SpritesPass(batch, shaderRegistry), layers, passDef.depthTest());
            case UI:
                return new UiGraphPass(passDef.id(), new UiRenderPass(ui, batch), passDef.depthTest());
            case CUSTOM:
                return new CustomGraphPass(
                        passDef.id(), passRegistry.require(passDef.handler()), viewport);
            case POST:
            case PARTICLES:
            case COMPUTE:
                return new UnimplementedGraphPass(passDef.id(), passDef.type());
            default:
                throw new PipelineParseException("unsupported pass type: " + passDef.type());
        }
    }

    private void validateCustomHandlers(PipelineDocument document) {
        for (PipelineDocument.PassDef pass : document.passes()) {
            if (pass.type() == PipelineDocument.PassType.CUSTOM
                    && passRegistry.find(pass.handler()).isEmpty()) {
                throw new PipelineParseException(
                        "custom pass \""
                                + pass.id()
                                + "\" references unregistered handler: "
                                + pass.handler());
            }
        }
    }

    private static Set<RenderLayer.Layer> parseLayers(List<String> layerNames) {
        EnumSet<RenderLayer.Layer> layers = EnumSet.noneOf(RenderLayer.Layer.class);
        for (String name : layerNames) {
            layers.add(parseLayer(name));
        }
        if (layers.isEmpty()) {
            layers.add(RenderLayer.Layer.WORLD);
        }
        return layers;
    }

    private static RenderLayer.Layer parseLayer(String name) {
        if (name == null || name.isBlank()) {
            return RenderLayer.Layer.WORLD;
        }
        try {
            return RenderLayer.Layer.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PipelineParseException("unknown render layer: " + name);
        }
    }
}
