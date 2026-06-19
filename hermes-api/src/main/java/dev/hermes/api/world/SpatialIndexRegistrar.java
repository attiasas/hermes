package dev.hermes.api.world;

/** Registry for custom {@link SpatialIndex} strategy factories (SPI). */
public interface SpatialIndexRegistrar {

    void register(String strategyId, SpatialIndexFactory factory);
}
