package dev.hermes.core.lighting;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.lighting.SceneLightingNames;
import dev.hermes.core.render.PipelineDocument;
import dev.hermes.core.render.PipelineLoader;

/** Applies render-pipeline light budgets to the active scene's {@link SceneLightingState}. */
public final class LightingBudgetResolver {

    private LightingBudgetResolver() {}

    public static void apply(WorldManager manager, String pipelineAssetPath) {
        PipelineDocument document = PipelineLoader.load(pipelineAssetPath);
        LightingBudgets budgets = PipelineDocument.maxWorld3dLightingBudgets(document);

        Entity scene = manager.entities().findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        if (scene == null) {
            return;
        }
        SceneLightingState state = manager.entities().getComponent(scene.id(), SceneLightingState.class);
        if (state == null) {
            return;
        }
        state.setMaxDirectional(budgets.maxDirectional());
        state.setMaxPoint(budgets.maxPoint());
        state.setMaxSpot(budgets.maxSpot());
    }
}
