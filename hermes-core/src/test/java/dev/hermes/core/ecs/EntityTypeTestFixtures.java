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
        registry.register(
                "SpinMarker",
                TestSpinMarker.class,
                (data, ctx) -> {
                    TestSpinMarker spin = new TestSpinMarker();
                    spin.setCenterX(data.getFloat("centerX", 0f));
                    spin.setCenterY(data.getFloat("centerY", 0f));
                    spin.setSpeed(data.getFloat("speed", 1f));
                    spin.setRadius(data.getFloat("radius", 1f));
                    return spin;
                });
        return registry;
    }
}
