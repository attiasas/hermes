# Scene format v1

Scene files describe entities and their components. They live under the game module assets directory (default `src/main/resources/assets/`, configurable via `hermes.assetsDirectory` in `game/build.gradle`) and are referenced from `hermes.json` via the `scene` field.

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
| `Transform` | See below | Position, rotation (degrees), and scale. All fields optional. |
| `Sprite` | `texture` (string) | Asset path relative to the assets root (or internal libGDX path). |
| `Camera` | See below | View/projection settings; pair with `Transform` on the same entity. |

### Transform properties

| Property | Default | Description |
|----------|---------|-------------|
| `x`, `y`, `z` | `0` | World position. For 2D scenes, omit `z`. |
| `rotationX`, `rotationY`, `rotationZ` | `0` | Euler rotation in degrees. `rotationZ` is used for 2D sprite drawing. |
| `scaleX`, `scaleY`, `scaleZ` | `1` | Scale per axis. `scaleX` / `scaleY` affect sprite size; `scaleZ` is stored for future 3D rendering. |

Sprites are drawn in **world space**. The active `Camera` entity (with a `Transform` on the same entity) defines view projection. Without a camera entity, the engine uses a default orthographic view centered on the viewport.

Example with 3D fields:

```json
"Transform": {
  "x": 100,
  "y": 200,
  "z": 5,
  "rotationZ": 45,
  "scaleX": 0.5,
  "scaleY": 0.5
}
```

### Camera properties

**Required:** a `Transform` on the same entity (camera position and rotation). Camera entities are never drawn, even if they also have a `Sprite`.

| Property | Default | Description |
|----------|---------|-------------|
| `projection` | `"orthographic"` | `"orthographic"` (2D) or `"perspective"` (3D) |
| `active` | `true` | When multiple cameras exist, the first active one is used |
| `zoom` | `1` | Orthographic zoom |
| `fieldOfView` | `67` | Perspective vertical FOV in degrees |
| `near`, `far` | `0.1`, `3000` | Clip planes |
| `viewportWidth`, `viewportHeight` | `0` | `0` = match window size |

Example:

```json
{
  "id": "main-camera",
  "components": {
    "Transform": { "x": 320, "y": 240, "z": 0 },
    "Camera": { "projection": "orthographic", "active": true, "zoom": 1 }
  }
}
```

Orthographic mode sorts drawables by world `z`. Perspective mode sorts by distance from the camera (farther first).

## Custom components

Register types in `onCreate(HermesEngine engine)` via `engine.registry().register(...)`, and add matching logic with `engine.addSystem(...)`. Alternatively, provide a `META-INF/services/dev.hermes.api.ecs.ComponentRegistration` implementation that registers both the component deserializer and any systems.

The sample game demonstrates both paths:

| Component | Registration | Behavior |
|-----------|--------------|----------|
| `SpinMarker` | ServiceLoader only | Orbits the entity around `centerX` / `centerY` at `radius` |
| `BounceMarker` | `onCreate` only | Vertical sine offset on top of the current `Transform.y` |

Unknown component type names fail at load time with a message naming the scene file, entity, and type.
