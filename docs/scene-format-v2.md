# Scene format v2

Scene files describe entities and their components. They live under the game module assets directory (default `src/main/resources/assets/`, configurable via `hermes.assetsDirectory` in `game/build.gradle`) and are referenced from `hermes.json` via the `scene` field.

**Breaking change from v1:** every entity with a `Sprite` or `Mesh` component **must** also include a `Material` component. Scenes without it fail at load time with `SceneParseException`.

## Top-level shape

```json
{
  "entities": [
    {
      "id": "logo",
      "components": {
        "Transform": { "x": 140, "y": 210 },
        "Sprite": { "texture": "hermes-logo.png" },
        "Material": { "shader": "default/unlit" }
      }
    }
  ]
}
```

## Fields

| Field | Required | Description |
|-------|----------|-------------|
| `entities` | No | Array of entity objects. Omitted or empty means an empty scene. |
| `entities[]` | — | Each element **must** be a JSON object. Non-objects fail at load time with `SceneParseException`. |
| `entities[].id` | No | Logical name for lookup and error messages. Duplicate names in the same world fail at runtime. |
| `entities[].kind` | No | Optional logical type tag (e.g. `"character"`, `"prop"`). Omitted entities use the unset kind. Used for `World.entitiesWithKind` and future save/load; does not affect component parsing. |
| `entities[].components` | No | Map of component type name → property object. |
| `renderPipeline` | No | Optional render pipeline asset path (e.g. `"render/ui-overlay.json"`). Overrides the project default from `hermes.json` for this scene only. Resolution order: scene JSON → `SceneDefinition.renderPipeline()` → project default. |

## Built-in component types

| Type | Properties | Description |
|------|------------|-------------|
| `Transform` | See below | Position, rotation (degrees), and scale. All fields optional. |
| `Sprite` | `texture` (string) | 2D texture path relative to the assets root. **Requires `Material` on the same entity.** |
| `Mesh` | `model` (string), `texture` (optional string) | 3D model path (e.g. Wavefront `.obj`) under the assets root; optional albedo texture. **Requires `Material` on the same entity.** |
| `Material` | `shader` (string), `uniforms` (optional object) | Shader id and optional uniform map (float arrays). Default shader: `default/unlit`. |
| `RenderLayer` | `layer` (string) | `"WORLD"` (default) or `"UI"` — world-space vs overlay draw order. |
| `Camera` | See below | View/projection settings; pair with `Transform` on the same entity. |

### Transform properties

| Property | Default | Description |
|----------|---------|-------------|
| `x`, `y`, `z` | `0` | World position. For 2D scenes, omit `z`. |
| `rotationX`, `rotationY`, `rotationZ` | `0` | Euler rotation in degrees. `rotationZ` is used for 2D sprite drawing. |
| `scaleX`, `scaleY`, `scaleZ` | `1` | Scale per axis. `scaleX` / `scaleY` affect sprite size; all axes apply to meshes. |

Sprites and meshes are drawn in **world space**. The active `Camera` entity (with a `Transform` on the same entity) defines view projection. Without a camera entity, the engine uses a default orthographic view centered on the viewport.

### Sprite + Material

```json
{
  "id": "logo",
  "components": {
    "Transform": { "x": 320, "y": 240 },
    "Sprite": { "texture": "hermes-logo.png" },
    "Material": { "shader": "default/unlit" }
  }
}
```

### Mesh + Material (3D)

Place models under `assets/models/` (e.g. `models/cube.obj`). Templates ship a minimal unit cube for experimentation.

```json
{
  "id": "cube",
  "components": {
    "Transform": { "z": 0 },
    "Mesh": { "model": "models/cube.obj" },
    "Material": { "shader": "default/unlit" }
  }
}
```

Optional texture on the mesh:

```json
"Mesh": { "model": "models/cube.obj", "texture": "brick.png" }
```

Use a perspective camera for 3D scenes:

```json
{
  "id": "cam",
  "components": {
    "Transform": { "x": 0, "y": 2, "z": 5 },
    "Camera": { "projection": "perspective", "active": true }
  }
}
```

### Material properties

| Property | Default | Description |
|----------|---------|-------------|
| `shader` | `default/unlit` | Built-in or registered shader id (path-style name). |
| `uniforms` | — | Map of uniform name → float array, e.g. `"u_tint": [1, 0, 0, 1]`. |

Example with tint:

```json
"Material": {
  "shader": "default/unlit",
  "uniforms": { "u_tint": [1, 0, 0, 1] }
}
```

### RenderLayer properties

| Property | Default | Description |
|----------|---------|-------------|
| `layer` | `"WORLD"` | `"WORLD"` for scene geometry/sprites, `"UI"` for screen-space overlays. |

### Camera properties

**Required:** a `Transform` on the same entity (camera position and rotation). Camera entities are never drawn, even if they also have a `Sprite` or `Mesh`.

| Property | Default | Description |
|----------|---------|-------------|
| `projection` | `"orthographic"` | `"orthographic"` (2D) or `"perspective"` (3D) |
| `active` | `true` | When multiple cameras exist, the first active one is used |
| `zoom` | `1` | Orthographic zoom |
| `fieldOfView` | `67` | Perspective vertical FOV in degrees |
| `near`, `far` | `0.1`, `3000` | Clip planes |
| `viewportWidth`, `viewportHeight` | `0` | `0` = match window size |
| `renderTarget` | *(unset)* | Optional pipeline framebuffer id; stored for future camera routing (see [render-pipeline.md](render-pipeline.md)) |

Orthographic mode sorts drawables by world `z`. Perspective mode sorts by distance from the camera (farther first).

## Migration from v1

Add a `Material` next to every `Sprite` and `Mesh`:

```json
"Material": { "shader": "default/unlit" }
```

No other v1 fields changed. See [scene-format-v1.md](scene-format-v1.md) for the retired v1-only doc (redirect).

## Custom components

Register types in `onCreate(HermesEngine engine)` via `engine.registry().register(...)`, and add matching logic with `engine.addSystem(...)`. Alternatively, provide a `META-INF/services/dev.hermes.api.ecs.ComponentRegistration` implementation that registers both the component deserializer and any systems.

Unknown component type names fail at load time with a message naming the scene file, entity, and type.
