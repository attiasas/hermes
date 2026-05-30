package dev.hermes.core.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dev.hermes.api.scene.SceneUiConfig;
import dev.hermes.api.ui.UiBindingProvider;
import dev.hermes.api.ui.UiDocument;
import dev.hermes.api.ui.UiService;
import dev.hermes.api.ui.UiWidgetRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** {@link UiService} implementation with per-scene widget trees and rendering. */
public final class UiServiceImpl implements UiService {

    private final UiDocumentLoader documentLoader = new UiDocumentLoader(new BuiltinUiWidgets());
    private final UiWidgetRegistryImpl widgets = new UiWidgetRegistryImpl();
    private final UiLayoutEngine layoutEngine = new UiLayoutEngine();
    private final UiFontRegistry fontRegistry = new UiFontRegistry();
    private final UiTextureCache textureCache = new UiTextureCache();
    private final UiTreeRenderer treeRenderer = new UiTreeRenderer(fontRegistry, textureCache);
    private final Map<String, Object> bindings = new HashMap<>();
    private final List<UiBindingProvider> bindingProviders = new ArrayList<>();
    private final Map<String, SceneUiState> sceneStates = new HashMap<>();

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
        if (key != null && !key.isBlank()) {
            bindings.put(key, value);
        }
    }

    @Override
    public Object getBinding(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (bindings.containsKey(key)) {
            return bindings.get(key);
        }
        for (UiBindingProvider provider : bindingProviders) {
            Optional<Object> resolved = provider.resolve(key);
            if (resolved.isPresent()) {
                return resolved.get();
            }
        }
        return null;
    }

    @Override
    public void addBindingProvider(UiBindingProvider provider) {
        if (provider != null) {
            bindingProviders.add(provider);
        }
    }

    @Override
    public UiWidgetRegistry widgets() {
        return widgets;
    }

    /**
     * Lays out and draws the active UI document for {@code sceneId} into {@code batch}.
     * Called from {@link dev.hermes.core.render.pass.UiRenderPass} each frame.
     */
    public void layoutAndRender(String sceneId, SpriteBatch batch, int surfaceWidth, int surfaceHeight) {
        if (sceneId == null
                || sceneId.isBlank()
                || batch == null
                || surfaceWidth <= 0
                || surfaceHeight <= 0) {
            return;
        }
        SceneUiState state = sceneStates.get(sceneId);
        if (state == null) {
            return;
        }
        UiDocument document = state.document();
        float scale = uiScale(state.config(), document, surfaceWidth, surfaceHeight);
        UiLayoutResult layout =
                layoutEngine.layout(document.root(), (int) document.designWidth(), (int) document.designHeight(), scale);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, surfaceWidth, surfaceHeight);
        batch.begin();
        treeRenderer.draw(document.root(), layout, batch, this::getBinding);
        batch.end();
    }

    public void dispose() {
        fontRegistry.dispose();
        textureCache.dispose();
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
