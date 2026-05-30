# World lighting

Hermes drives 3D mesh lighting from scene JSON, entity templates, and render pipeline budgets. No Java is required for
outdoor sun, dungeon torches, or ambient fill. Meshes with vertex normals use the `default/lit` shader; flat albedo
without lighting uses `default/unlit`.

See also: [scene-format-v1.md](scene-format-v1.md) (lighting block and light components),
[render-pipeline.md](render-pipeline.md) (pass light budgets), [entity-types.md](entity-types.md) (torch templates).

## Complexity tiers

| Tier | You write | Engine does |
|------|-----------|-------------|
| **0 — Defaults** | Nothing (omit `lighting`) | Engine ambient + directional defaults (same look as pre-lighting Hermes) |
| **1 — Scene block** | Top-level `"lighting": { ... }` | Ambient + default directional without light entities |
| **2 — Light entities** | `"type": "torch"` or inline light components | Transform-driven positions and directions; template merge |
| **3 — Dynamic / custom** | Java systems mutating light components; custom shaders + pipeline budgets | `BuiltinLightingSystem` recompiles each frame; `ShaderRegistry` matches pass caps |

## Shaders

| Shader id | Use for | Behavior |
|-----------|---------|----------|
| `default/lit` | 3D `Mesh` entities with vertex normals | Forward-lit `default.vert` / `default.frag` |
| `default/unlit` | Sprites, UI-style meshes, emissive-only props | Flat albedo — `default-unlit.frag` ignores lights |

Register both in `assets/render/pipeline.json`:

```json
"shaders": {
  "default/lit": {
    "vertex": "shaders/default.vert",
    "fragment": "shaders/default.frag"
  },
  "default/unlit": {
    "vertex": "shaders/default.vert",
    "fragment": "shaders/default-unlit.frag"
  }
}
```

## Tier 0 — Engine defaults

Omit the `lighting` block entirely. Any 3D mesh using `default/lit` receives ambient `{0.4, 0.4, 0.4}` and a
directional light aimed at `[-1, -0.8, -0.2]`.

## Tier 1 — Scene lighting block

Set ambient and sun in scene JSON without spawning light entities:

```json
{
  "lighting": {
    "version": 1,
    "ambient": { "color": [0.25, 0.28, 0.35, 1] },
    "directional": {
      "color": [1, 0.92, 0.75, 1],
      "intensity": 1.15,
      "direction": [-0.5, -1, -0.2]
    }
  },
  "entities": [
    {
      "id": "cam",
      "components": {
        "Transform": { "y": 2, "z": 6 },
        "Camera": { "projection": "perspective", "active": true }
      }
    },
    {
      "id": "ground",
      "components": {
        "Transform": { "y": -1 },
        "Mesh": { "model": "models/cube.obj", "texture": "grass.png" },
        "Material": { "shader": "default/lit" }
      }
    }
  ]
}
```

## Tier 2 — Light entity templates

Reusable torch template at `assets/entities/torch/type.json`:

```json
{
  "version": 1,
  "components": {
    "Transform": { "y": 0.5 },
    "PointLight": {
      "color": [1, 0.6, 0.2, 1],
      "intensity": 2,
      "range": 8
    }
  }
}
```

Dungeon-style scene with low ambient and torch instances:

```json
{
  "lighting": {
    "version": 1,
    "ambient": { "color": [0.08, 0.08, 0.12, 1], "intensity": 1 }
  },
  "entities": [
    { "type": "torch", "id": "t1", "components": { "Transform": { "x": -2, "z": 0 } } },
    { "type": "torch", "id": "t2", "components": { "Transform": { "x": 2, "z": 0 } } }
  ]
}
```

Point and spot lights need pipeline budgets (see below). For many torches, raise `maxPoint` on the `world3d` pass.

Inline sun entity (directional light aims along entity **−Z** after rotation unless `direction` is set):

```json
{
  "id": "sun",
  "components": {
    "Transform": { "rotationX": -45, "rotationY": 30 },
    "DirectionalLight": {
      "color": [1, 0.95, 0.8, 1],
      "intensity": 1.2
    }
  }
}
```

## Tier 3 — Java and custom shaders

Register systems with `SystemScope.ACTIVE_SCENE` to animate lights at runtime:

```java
public final class DayNightSystem implements System {
    @Override
    public void update(WorldManager manager, float deltaSeconds) {
        Entity sun = manager.entities().findByName("sun");
        if (sun == null) return;
        DirectionalLight light = manager.entities().getComponent(sun.id(), DirectionalLight.class);
        if (light == null) return;
        float t = (float) Math.sin(worldTime);
        light.setIntensity(0.4f + 0.6f * Math.max(0f, t));
    }
}
```

Custom GLSL shaders must declare libGDX lighting uniforms consistent with pass `lighting` budgets. Game modules that
depend on libGDX may read the compiled environment via `LightingRuntime.require(entities)` in custom render passes.

## Pipeline light budgets

Declare caps on each `world3d` pass so the shader compiler allocates enough light slots:

```json
{
  "id": "world3d",
  "type": "world3d",
  "target": "screen",
  "layers": ["WORLD"],
  "lighting": {
    "maxDirectional": 1,
    "maxPoint": 8,
    "maxSpot": 0
  }
}
```

Omitted `lighting` defaults to `{ maxDirectional: 1, maxPoint: 0, maxSpot: 0 }`. When point/spot counts exceed the
budget, lights nearest the active camera are kept.

High point counts are expensive on WebGL; prefer lower `maxPoint` for HTML builds.

## Dogfood lit demo

`dogfood-simulation` includes `assets/scenes/lit-demo.json` — outdoor directional sun, a spinning lit cube, and two
torch point lights. Run manually by setting `"scene": "scenes/lit-demo.json"` in `hermes.json` (entry scene is unchanged
by default).

## Out of scope (v1)

Shadow maps, image-based ambient, 2D sprite lighting, light probes, and debug light gizmos are planned extensions.
Light component fields and `SceneLightingState` are the stable hooks for save/load and debug tooling later.
