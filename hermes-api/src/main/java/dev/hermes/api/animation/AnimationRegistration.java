package dev.hermes.api.animation;

import dev.hermes.api.ecs.HermesEngine;

/** SPI hook for registering animation resolvers and backends. */
public interface AnimationRegistration {

    void register(HermesEngine engine, AnimationRegistrar registrar);
}
