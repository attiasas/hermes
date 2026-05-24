package dev.hermes.api.config;

/**
 * Programmatic runtime configuration overrides applied at startup before the engine boots.
 */
public interface RuntimeConfigBuilder {

    void put(String key, String value);
}
