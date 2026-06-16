package dev.hermes.api.world;

/** SPI hook for registering custom spatial index strategies by id. */
public interface SpatialIndexRegistration {

    void register(SpatialIndexRegistrar registrar);
}
