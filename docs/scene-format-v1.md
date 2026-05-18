# Scene format v1

Scene files describe entities and their components. They live under the shared `assets/` directory and are referenced from `hermes.json` via the `scene` field.

## Top-level shape

```json
{
  "entities": [
    {
      "id": "logo",
      "components": {
        "Transform": { "x": 140, "y": 210 },
        "Sprite": { "texture": "libgdx.png" }
      }
    }
  ]
}
```

## Fields

| Field | Required | Description |
|-------|----------|-------------|
| `entities` | No | Array of entity objects. Omitted or empty means an empty scene. |
| `entities[].id` | No | Logical name for lookup and error messages. |
| `entities[].components` | No | Map of component type name → property object. |

## Built-in component types

| Type | Properties | Description |
|------|------------|-------------|
| `Transform` | `x`, `y` (numbers, default 0) | 2D position in pixels. |
| `Sprite` | `texture` (string) | Asset path relative to `assets/` (or internal libGDX path). |

## Custom components

Register types in `onCreate(HermesEngine engine)` via `engine.registry().register(...)`, and add matching logic with `engine.addSystem(...)`. Alternatively, provide a `META-INF/services/dev.hermes.api.ecs.ComponentRegistration` implementation that registers both the component deserializer and any systems.

The sample game demonstrates both paths:

| Component | Registration | Behavior |
|-----------|--------------|----------|
| `SpinMarker` | ServiceLoader only | Orbits the entity around `centerX` / `centerY` at `radius` |
| `BounceMarker` | `onCreate` only | Vertical sine offset on top of the current `Transform.y` |

Unknown component type names fail at load time with a message naming the scene file, entity, and type.
