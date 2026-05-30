package dev.hermes.core.ecs;

final class EntityTypeTestFixtures {

    private EntityTypeTestFixtures() {
    }

    static EntityTypeRegistryImpl registryWithSpinCube() {
        EntityTypeRegistryImpl registry = new EntityTypeRegistryImpl();
        registry.scanTestAssets("entities/spin-cube/type.json");
        return registry;
    }

    static ComponentRegistryImpl testRegistryWithMeshMaterialTransform() {
        ComponentRegistryImpl registry = new ComponentRegistryImpl();
        BuiltinComponents.register(registry);
        return registry;
    }
}
