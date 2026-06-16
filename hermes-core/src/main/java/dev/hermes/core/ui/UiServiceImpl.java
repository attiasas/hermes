package dev.hermes.core.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.Entity;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.UiAttach;
import dev.hermes.api.math.Rect4;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.api.ui.UiAnchor;
import dev.hermes.api.ui.UiBindingProvider;
import dev.hermes.api.ui.UiDocument;
import dev.hermes.api.ui.UiNode;
import dev.hermes.api.ui.UiService;
import dev.hermes.api.ui.UiWidgetRegistry;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.resource.ResourceManagerImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** {@link UiService} implementation with per-scene widget trees and rendering. */
public final class UiServiceImpl implements UiService {

    private final UiWidgetRegistryImpl widgets = new UiWidgetRegistryImpl();
    private final UiWidgetTypes widgetTypes = new UiWidgetTypes(new BuiltinUiWidgets(), widgets);
    private final UiDocumentLoader documentLoader = new UiDocumentLoader(widgetTypes);
    private final UiLayoutEngine layoutEngine = new UiLayoutEngine();
    private final UiFontRegistry fontRegistry = new UiFontRegistry();
    private final UiTreeRenderer treeRenderer;
    private final UiBindingResolver bindingResolver = new UiBindingResolver();
    private final Map<String, SceneUiState> sceneStates = new HashMap<>();
    private final Map<EntityId, AttachAnchor> attachAnchors = new HashMap<>();

    public UiServiceImpl(ResourceManagerImpl resources) {
        java.util.Objects.requireNonNull(resources, "resources");
        this.treeRenderer = new UiTreeRenderer(fontRegistry, resources, widgets);
    }

    @Override
    public UiDocument load(String assetPath) {
        return documentLoader.load(assetPath);
    }

    @Override
    public void onSceneEnter(String sceneId, Optional<SceneUiConfig> config) {
        if (sceneId == null || sceneId.isBlank()) {
            return;
        }
        config.ifPresent(
                c -> sceneStates.put(sceneId, new SceneUiState(documentLoader.load(c.document()), c)));
    }

    @Override
    public void onSceneExit(String sceneId) {
        if (sceneId != null) {
            sceneStates.remove(sceneId);
        }
    }

    @Override
    public void setBinding(String key, Object value) {
        bindingResolver.setBinding(key, value);
    }

    @Override
    public Object getBinding(String key) {
        return bindingResolver.resolve(key);
    }

    @Override
    public void addBindingProvider(UiBindingProvider provider) {
        bindingResolver.addProvider(provider);
    }

    @Override
    public UiWidgetRegistry widgets() {
        return widgets;
    }

    /**
     * Lays out and draws the active UI document for {@code sceneId} into {@code batch}.
     * Called from {@link dev.hermes.core.render.pass.UiRenderPass} each frame.
     */
    public Optional<LaidOutSceneUi> layoutScene(String sceneId, int surfaceWidth, int surfaceHeight) {
        if (sceneId == null
                || sceneId.isBlank()
                || surfaceWidth <= 0
                || surfaceHeight <= 0) {
            return Optional.empty();
        }
        SceneUiState state = sceneStates.get(sceneId);
        if (state == null) {
            return Optional.empty();
        }
        UiDocument document = state.document();
        float scale = uiScale(state.config(), document, surfaceWidth, surfaceHeight);
        UiLayoutResult layout =
                layoutEngine.layout(document.root(), (int) document.designWidth(), (int) document.designHeight(), scale);
        return Optional.of(new LaidOutSceneUi(document.root(), layout));
    }

    public void layoutAndRender(
            String sceneId, EntityStore entities, SpriteBatch batch, int surfaceWidth, int surfaceHeight) {
        if (batch == null || surfaceWidth <= 0 || surfaceHeight <= 0) {
            return;
        }
        batch.getProjectionMatrix().setToOrtho2D(0, 0, surfaceWidth, surfaceHeight);
        batch.begin();
        layoutScene(sceneId, surfaceWidth, surfaceHeight)
                .ifPresent(
                        laidOut ->
                                treeRenderer.draw(
                                        laidOut.root(), laidOut.layout(), batch, bindingResolver::resolve));
        if (entities != null) {
            for (Entity entity : entities.entitiesWith(UiAttach.class)) {
                tryLayoutAttach(entity.id(), surfaceWidth, surfaceHeight)
                        .ifPresent(
                                laidOut ->
                                        treeRenderer.draw(
                                                laidOut.root(),
                                                laidOut.layout(),
                                                batch,
                                                bindingResolver::resolve));
            }
        }
        batch.end();
    }

    /** Updates SURFACE anchors for visible {@link UiAttach} entities (called by {@link UiAttachSystem}). */
    public void updateAttachLayouts(EntityStore entities, ViewportService viewport) {
        attachAnchors.clear();
        if (entities == null || viewport == null) {
            return;
        }
        Vec2 screen = new Vec2();
        for (Entity entity : entities.entitiesWith(UiAttach.class)) {
            UiAttach attach = entities.getComponent(entity.id(), UiAttach.class);
            if (attach == null || !attach.visible()) {
                continue;
            }
            String documentPath = attach.document();
            if (documentPath == null || documentPath.isBlank()) {
                continue;
            }
            Transform transform = resolveFollowTransform(entities, entity, attach);
            if (transform == null) {
                continue;
            }
            float worldX = transform.x() + attach.offsetX();
            float worldY = transform.y() + attach.offsetY();
            float worldZ = transform.z() + attach.offsetZ();
            viewport.worldToScreen(entities, worldX, worldY, worldZ, screen);
            attachAnchors.put(entity.id(), new AttachAnchor(screen.x, screen.y, documentPath));
        }
    }

    private Optional<LaidOutSceneUi> tryLayoutAttach(EntityId entityId, int surfaceWidth, int surfaceHeight) {
        if (entityId == null || surfaceWidth <= 0 || surfaceHeight <= 0) {
            return Optional.empty();
        }
        AttachAnchor anchor = attachAnchors.get(entityId);
        if (anchor == null) {
            return Optional.empty();
        }
        UiDocument document = documentLoader.load(anchor.documentPath);
        float scale = 1f;
        UiLayoutResult base =
                layoutEngine.layout(
                        document.root(),
                        (int) document.designWidth(),
                        (int) document.designHeight(),
                        scale);
        UiLayoutResult shifted = shiftLayoutToScreenAnchor(base, document.root(), anchor.screenX, anchor.screenY);
        return Optional.of(new LaidOutSceneUi(document.root(), shifted));
    }

    Optional<LaidOutSceneUi> layoutAttach(EntityId entityId, int surfaceWidth, int surfaceHeight) {
        return tryLayoutAttach(entityId, surfaceWidth, surfaceHeight);
    }

    private static Transform resolveFollowTransform(EntityStore entities, Entity attachEntity, UiAttach attach) {
        String follow = attach.follow();
        if (follow == null || follow.isBlank()) {
            return entities.getComponent(attachEntity.id(), Transform.class);
        }
        Entity target = entities.findByName(follow);
        if (target == null) {
            return null;
        }
        return entities.getComponent(target.id(), Transform.class);
    }

    private static UiLayoutResult shiftLayoutToScreenAnchor(
            UiLayoutResult layout, UiNode root, float anchorX, float anchorY) {
        Rect4 rootBounds = layout.bounds(root.id());
        UiAnchor anchor = root.layout().anchor();
        float rootAnchorX = anchorPointX(rootBounds, anchor);
        float rootAnchorY = anchorPointY(rootBounds, anchor);
        float dx = anchorX - rootAnchorX;
        float dy = anchorY - rootAnchorY;
        Map<String, Rect4> shifted = new HashMap<>();
        for (Map.Entry<String, Rect4> entry : layout.boundsById().entrySet()) {
            Rect4 b = entry.getValue();
            shifted.put(entry.getKey(), new Rect4(b.x + dx, b.y + dy, b.width, b.height));
        }
        return new UiLayoutResult(shifted);
    }

    private static float anchorPointX(Rect4 bounds, UiAnchor anchor) {
        if (anchor == UiAnchor.TOP_CENTER || anchor == UiAnchor.CENTER || anchor == UiAnchor.BOTTOM_CENTER) {
            return bounds.x + bounds.width * 0.5f;
        }
        if (anchor == UiAnchor.TOP_RIGHT || anchor == UiAnchor.CENTER_RIGHT || anchor == UiAnchor.BOTTOM_RIGHT) {
            return bounds.x + bounds.width;
        }
        return bounds.x;
    }

    private static float anchorPointY(Rect4 bounds, UiAnchor anchor) {
        if (anchor == UiAnchor.CENTER_LEFT || anchor == UiAnchor.CENTER || anchor == UiAnchor.CENTER_RIGHT) {
            return bounds.y + bounds.height * 0.5f;
        }
        if (anchor == UiAnchor.TOP_LEFT || anchor == UiAnchor.TOP_CENTER || anchor == UiAnchor.TOP_RIGHT) {
            return bounds.y + bounds.height;
        }
        return bounds.y;
    }

    public void dispose() {
        fontRegistry.dispose();
        treeRenderer.disposeWhitePixel();
    }

    private static float uiScale(SceneUiConfig config, UiDocument document, int surfaceW, int surfaceH) {
        float designW = document.designWidth();
        float designH = document.designHeight();
        if (designW <= 0f || designH <= 0f) {
            return 1f;
        }
        float scaleX = surfaceW / designW;
        float scaleY = surfaceH / designH;
        String fitMode = config.fitMode();
        if ("stretch".equalsIgnoreCase(fitMode)) {
            return scaleX;
        }
        if ("fill".equalsIgnoreCase(fitMode)) {
            return Math.max(scaleX, scaleY);
        }
        return Math.min(scaleX, scaleY);
    }

    public static final class LaidOutSceneUi {
        private final UiNode root;
        private final UiLayoutResult layout;

        LaidOutSceneUi(UiNode root, UiLayoutResult layout) {
            this.root = root;
            this.layout = layout;
        }

        public UiNode root() {
            return root;
        }

        public UiLayoutResult layout() {
            return layout;
        }
    }

    private static final class SceneUiState {
        private final UiDocument document;
        private final SceneUiConfig config;

        SceneUiState(UiDocument document, SceneUiConfig config) {
            this.document = document;
            this.config = config;
        }

        UiDocument document() {
            return document;
        }

        SceneUiConfig config() {
            return config;
        }
    }

}
