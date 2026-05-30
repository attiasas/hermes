package dev.hermes.core.lighting;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Vector3;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.SceneLightingState;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.System;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.lighting.SceneLightingNames;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.viewport.BackbufferSize;

/** ACTIVE_SCENE system: gathers ECS lights into a libGDX {@link Environment} each frame. */
public final class BuiltinLightingSystem implements System {

    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        EntityStore entities = manager.entities();
        Entity scene = entities.findByName(SceneLightingNames.SCENE_ENTITY_NAME);
        if (scene == null) {
            return;
        }
        SceneLightingState state = entities.getComponent(scene.id(), SceneLightingState.class);
        if (state == null) {
            return;
        }

        ActiveCamera cam = CameraResolver.resolve(entities, BackbufferSize.width(), BackbufferSize.height());
        Vector3 camPos = new Vector3(cam.x(), cam.y(), cam.z());
        Environment env =
                LightEnvironmentBuilder.build(entities, state, LightingBudgets.from(state), camPos);
        LightingRuntime.publish(entities, env);
        state.bumpRevision();
    }
}
