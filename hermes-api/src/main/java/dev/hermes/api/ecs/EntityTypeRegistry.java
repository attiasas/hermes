package dev.hermes.api.ecs;

/**
 * Registry of reusable entity type templates loaded from game assets.
 */
public interface EntityTypeRegistry {

    void scanAssets();

    void register(String kind, String assetPath);

    boolean has(String kind);

    EntityTypeDefinition require(String kind);
}
