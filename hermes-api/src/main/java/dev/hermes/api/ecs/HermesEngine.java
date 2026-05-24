package dev.hermes.api.ecs;

import dev.hermes.api.config.RuntimeConfigService;
import dev.hermes.api.scene.SceneManager;

/**
 * Runtime engine context exposed to user applications during bootstrap.
 */
public interface HermesEngine {

    SceneManager scenes();

    ComponentRegistry registry();

    void addSystem(System system);

    void addSystem(System system, SystemScope scope);

    RuntimeConfigService runtimeConfig();
}
