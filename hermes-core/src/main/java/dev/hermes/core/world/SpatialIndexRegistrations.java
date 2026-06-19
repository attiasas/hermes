package dev.hermes.core.world;

import dev.hermes.api.world.SpatialIndexRegistration;

import java.util.ServiceLoader;

/** Loads {@link SpatialIndexRegistration} SPI entries. */
public final class SpatialIndexRegistrations {

    private static final SpatialIndexStrategyRegistry REGISTRY = new SpatialIndexStrategyRegistry();

    private SpatialIndexRegistrations() {}

    public static SpatialIndexStrategyRegistry registry() {
        return REGISTRY;
    }

    public static void loadServiceRegistrations() {
        for (SpatialIndexRegistration registration : ServiceLoader.load(SpatialIndexRegistration.class)) {
            registration.register(REGISTRY);
        }
    }
}
