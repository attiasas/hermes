# Scene format v1

Scene files describe entities and their components. They live under the game module assets directory (default
`src/main/resources/assets/`, configurable via `hermes.assetsDirectory` in the game module's `build.gradle`) and are referenced from
`hermes.json` via the `scene` field.

Hermes is **pre-release**; the scene format stays **v1** until 1.0. JSON rules may change — update this doc in the same
PR when they do.

Every entity with a `Sprite` or `Mesh` component **must** also include a `Material` component. Scenes without it fail at
load time with `SceneParseException`.

## Top-level shape

```json
{
  "ui": "ui/hud.json",
  "inputContext": "gameplay",
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

| Field                   | Required | Description                                                                                                                                                                                                                       |
|-------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ui`                    | No       | Screen-space UI document: asset path string (e.g. `"ui/menu.json"`) or object with `document`, optional `fitMode` (`fit`, `stretch`, `fill`), optional `designAspect`. See [ui-format-v1.md](ui-format-v1.md).                    |
| `inputContext`          | No       | Overrides the input profile default context while this scene is active (top of stack). Non-empty string. See [input-format-v1.md](input-format-v1.md).                                                                            |
| `entities`              | No       | Array of entity objects. Omitted or empty means an empty scene.                                                                                                                                                                   |
| `entities[]`            | —        | Each element **must** be a JSON object. Non-objects fail at load time with `SceneParseException`.                                                                                                                                 |
| `entities[].id`         | No       | Logical name for lookup and error messages. Duplicate names in the same scene fail at runtime.                                                                                                                                    |
| `entities[].type`       | No       | Entity template kind (preferred). Loads `assets/entities/<kind>/type.json` when registered, then merges scene `components`. See [entity-types.md](entity-types.md).                                                              |
| `entities[].kind`       | No       | Alias for `type`. If both are set, `type` wins. Unregistered kind → tag-only (`EntityKind` stored, no template merge).                                                                                                          |
| `entities[].components` | No       | Map of component type name → property object. Deep-merged on top of the type template when `"type"`/`"kind"` is set and registered.                                                                                               |
| `renderPipeline`        | No       | Optional render pipeline asset path (e.g. `"render/ui-overlay.json"`). Overrides the project default from `hermes.json` for this scene only. Resolution order: scene JSON → `SceneDefinition.renderPipeline()` → project default. |
| `lighting`              | No       | Scene-wide lighting defaults (version 1). See [World lighting](world-lighting.md) and [Lighting block](#lighting-block) below. |
| `audio`                 | No       | Scene background music and fade settings. See [Audio block](#audio-block) and [audio.md](audio.md). |

## Built-in component types

| Type          | Properties                                      | Description                                                                                                                       |
|---------------|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `Transform`   | See below                                       | Position, rotation (degrees), and scale. All fields optional.                                                                     |
| `Sprite`      | `texture` (string)                              | 2D texture path relative to the assets root. **Requires `Material` on the same entity.**                                          |
| `Mesh`        | `model` (string), `texture` (optional string)   | 3D model path (e.g. Wavefront `.obj`) under the assets root; optional albedo texture. **Requires `Material` on the same entity.** |
| `Material`    | `shader` (string), `uniforms` (optional object) | Shader id and optional uniform map (float arrays). Default shader: `default/unlit`. Use `default/lit` for lit 3D meshes.          |
| `RenderLayer` | `layer` (string)                                | `"WORLD"` (default). Screen-space UI uses the scene `"ui"` field (widget trees), not `RenderLayer`.                              |
| `Selectable`  | See below                                       | Marks entity as screen-pickable; pair with `Transform`. Used by built-in selection and drag systems.                              |
| `Selected`    | —                                               | Runtime marker for the currently selected entity (usually set by `SelectionSystem`, not authored in JSON).                      |
| `UiAttach`    | See below                                       | World-attached UI overlay (`document`, `follow` entity id). No `Sprite` or `RenderLayer` required.                               |
| `Camera`      | See below                                       | View/projection settings; pair with `Transform` on the same entity.                                                               |
| `AmbientLight` | See [Light components](#light-components)      | Global ambient fill on a light entity (typically one). Position from `Transform` is ignored.                                     |
| `DirectionalLight` | See [Light components](#light-components)  | Sun/moon; direction from entity **−Z** after rotation, or explicit `direction`. Pair with `Transform` for animated sun.        |
| `PointLight`  | See [Light components](#light-components)       | Local lamp; position from `Transform`. Requires pipeline `maxPoint` budget.                                                      |
| `SpotLight`   | See [Light components](#light-components)       | Cone light; position and aim from `Transform` (**−Z** or `direction` override). Requires pipeline `maxSpot` budget.              |
| `AmbientSource` | See [Audio components](#audio-components)     | 3D positional looping ambient SFX. Requires `Transform`. See [audio.md](audio.md).                                              |
| `SoundEmitter` | See [Audio components](#audio-components)    | One-shot or looped SFX with `spawn` / `interval` / `manual` triggers.                                                           |
| `FootstepEmitter` | See [Audio components](#audio-components) | Footstep clips driven by entity movement. Requires `Transform`.                                                                 |

### Transform properties

| Property                              | Default | Description                                                                       |
|---------------------------------------|---------|-----------------------------------------------------------------------------------|
| `x`, `y`, `z`                         | `0`     | World position. For 2D scenes, omit `z`.                                          |
| `rotationX`, `rotationY`, `rotationZ` | `0`     | Euler rotation in degrees. `rotationZ` is used for 2D sprite drawing.             |
| `scaleX`, `scaleY`, `scaleZ`          | `1`     | Scale per axis. `scaleX` / `scaleY` affect sprite size; all axes apply to meshes. |

Sprites and meshes are drawn in **world space**. The active `Camera` entity (with a `Transform` on the same entity)
defines view projection. Without a camera entity, the engine uses a default orthographic view centered on the viewport.
See [coordinate-spaces.md](coordinate-spaces.md) for SCREEN / SURFACE / WORLD rules and `engine.viewport()`.

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
    "Material": { "shader": "default/lit" }
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

| Property   | Default         | Description                                                       |
|------------|-----------------|-------------------------------------------------------------------|
| `shader`   | `default/unlit` | Built-in or registered shader id. Use `default/lit` for forward-lit 3D meshes; `default/unlit` for flat albedo (sprites, unlit props). |
| `uniforms` | —               | Map of uniform name → float array, e.g. `"u_tint": [1, 0, 0, 1]`. |

Example with tint:

```json
"Material": {
  "shader": "default/unlit",
  "uniforms": { "u_tint": [1, 0, 0, 1] }
}
```

### RenderLayer properties

| Property | Default   | Description                                                             |
|----------|-----------|-------------------------------------------------------------------------|
| `layer`  | `"WORLD"` | World-space draw order for sprites and meshes.                            |

### Selectable properties

Pair with `Transform` for pick center. Used by `InputService.pick` and built-in `SelectionSystem` / camera / drag systems.
See [input.md](input.md).

| Property  | Default   | Description                                                                                    |
|-----------|-----------|------------------------------------------------------------------------------------------------|
| `enabled` | `true`    | When `false`, entity is skipped by picking.                                                    |
| `radius`  | `16`      | Pick radius in world units (circle in XY for ortho; sphere for perspective).                   |
| `layer`   | `"WORLD"` | Must be `"WORLD"`; gameplay picking uses `PickLayer.WORLD` only.                             |

```json
"Selectable": { "radius": 48, "layer": "WORLD" }
```

### Selected

Empty marker component. `SelectionSystem` adds it to the picked entity and clears any previous `Selected`. Games may read
`manager.entities().entitiesWith(Selected.class)` for highlighting or custom logic. Not typically placed in scene JSON.

### Scene UI (`ui` field)

Full-screen HUDs, menus, and pause overlays use a top-level `"ui"` field — not `Sprite` entities or a `ui-camera`.

Shorthand (defaults `fitMode: fit`, aspect from document `designSize`):

```json
{
  "ui": "ui/main-menu.json",
  "inputContext": "menu",
  "entities": []
}
```

Object form:

```json
{
  "ui": {
    "document": "ui/main-menu.json",
    "fitMode": "fit",
    "designAspect": 1.777
  },
  "inputContext": "menu",
  "entities": []
}
```

Widget JSON, bindings, and author tiers: [ui-format-v1.md](ui-format-v1.md). The render pipeline `ui` pass draws the active scene tree (see [render-pipeline.md](render-pipeline.md)).

### UiAttach properties

World-attached overlays (HP bars, nameplates) use a marker entity with `UiAttach` only — position comes from `follow` + offsets.

| Property   | Default | Description                                                          |
|------------|---------|----------------------------------------------------------------------|
| `document` | —       | UI asset path under `assets/` (required).                            |
| `follow`   | —       | Target entity `id` in this scene (`findByName`).                     |
| `offsetX`  | `0`     | World offset before screen projection.                               |
| `offsetY`  | `0`     | World offset before screen projection.                               |
| `offsetZ`  | `0`     | World offset before screen projection.                               |
| `visible`  | `true`  | When `false`, attach is not updated or drawn.                          |

```json
{
  "id": "player-hp",
  "components": {
    "UiAttach": {
      "document": "ui/hp-bar.json",
      "follow": "player",
      "offsetY": 2.2
    }
  }
}
```

Pair dynamic values with `engine.ui().setBinding(...)` (tier 2). Do not use `RenderLayer` or `Selectable.layer: "UI"` for HUD chrome.

### Camera properties

**Required:** a `Transform` on the same entity (camera position and rotation). Camera entities are never drawn, even if
they also have a `Sprite` or `Mesh`.

| Property                          | Default          | Description                                                                                                       |
|-----------------------------------|------------------|-------------------------------------------------------------------------------------------------------------------|
| `projection`                      | `"orthographic"` | `"orthographic"` (2D) or `"perspective"` (3D)                                                                     |
| `active`                          | `true`           | When multiple cameras exist, the first active one is used                                                         |
| `zoom`                            | `1`              | Orthographic zoom                                                                                                 |
| `fieldOfView`                     | `67`             | Perspective vertical FOV in degrees                                                                               |
| `near`, `far`                     | `0.1`, `3000`    | Clip planes                                                                                                       |
| `viewportWidth`, `viewportHeight` | `0`              | `0` = full render surface for the pass (not window size when targeting an FBO)                                    |
| `renderTarget`                    | *(unset)*        | Pipeline framebuffer id; camera applies when that pass target runs (see [render-pipeline.md](render-pipeline.md)) |
| `fitMode`                         | `"letterbox"`    | `"stretch"` \| `"letterbox"` \| `"crop"` \| `"fixed"` — aspect policy on the render surface                      |
| `designAspect`                    | `0`              | Width/height ratio for fit; `0` = use surface aspect                                                              |
| `lookAt`                          | *(unset)*        | `{ "x", "y", "z" }` world point for perspective aim (optional)                                                  |

Orthographic mode sorts drawables by world `z`. Perspective mode sorts by distance from the camera (farther first).

## Lighting block

Optional top-level `"lighting"` object sets scene-wide defaults without spawning light entities. Omitted entirely →
engine defaults (Tier 0). Full guide: [world-lighting.md](world-lighting.md).

```json
{
  "lighting": {
    "version": 1,
    "ambient": { "color": [0.35, 0.35, 0.4, 1], "intensity": 1 },
    "directional": {
      "color": [1, 0.95, 0.85, 1],
      "intensity": 1.1,
      "direction": [-0.4, -1, -0.3]
    },
    "point": [
      { "position": [0, 2, 0], "color": [1, 0.8, 0.5, 1], "intensity": 1.5, "range": 12 }
    ],
    "spot": [
      {
        "position": [0, 3, 0],
        "direction": [0, -1, 0],
        "color": [1, 1, 1, 1],
        "intensity": 1,
        "range": 10,
        "cutoffAngle": 45,
        "exponent": 1
      }
    ]
  },
  "entities": []
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `lighting.version` | Yes | Must be `1`. |
| `lighting.ambient` | No | `{ "color": [r,g,b,a], "intensity": float }` — default ambient when no `AmbientLight` entity wins. |
| `lighting.directional` | No | Default sun; omitted → engine default directional. |
| `lighting.point` | No | Static point lights (no entities). Each entry: `position`, `color`, `intensity`, `range`. |
| `lighting.spot` | No | Static spot lights. Each entry: `position`, `direction`, `color`, `intensity`, `range`, `cutoffAngle`, `exponent`. |

For moving or flickering lights, use light **entities** with `Transform` (see below).

### Light components

Light entities do not require `Mesh` or `Sprite` — they are invisible unless you add debug geometry later.

Shared optional fields on all light components: `enabled` (default `true`), `intensity` (default `1`), `color` (RGBA
float array, alpha ignored).

**AmbientLight** — global fill; `Transform` is ignored.

```json
"AmbientLight": { "color": [0.2, 0.2, 0.25, 1], "intensity": 0.8 }
```

**DirectionalLight** — aims along entity local **−Z** in world space after `Transform` rotation, unless `direction`
`[x,y,z]` is set.

```json
"DirectionalLight": {
  "color": [1, 0.95, 0.8, 1],
  "intensity": 1.2,
  "direction": [-0.5, -1, -0.2]
}
```

**PointLight** — position from `Transform`; `range` default `10`.

```json
"PointLight": { "color": [1, 0.6, 0.2, 1], "intensity": 2, "range": 8 }
```

**SpotLight** — position from `Transform`; direction **−Z** or `direction` override; `cutoffAngle` default `45`,
`exponent` default `1`, `range` default `10`.

```json
"SpotLight": {
  "color": [1, 1, 0.9, 1],
  "intensity": 1.5,
  "range": 12,
  "cutoffAngle": 35,
  "exponent": 2
}
```

Point and spot lights require matching `maxPoint` / `maxSpot` budgets on the render pipeline `world3d` pass — see
[render-pipeline.md](render-pipeline.md).

## Audio block

Optional top-level `"audio"` object drives background music when the scene enters the stack. Full guide: [audio.md](audio.md).

```json
{
  "audio": {
    "bgm": "overworld",
    "fadeInSeconds": 1.5,
    "fadeOutSeconds": 1.0,
    "pauseBgmOnPause": false
  },
  "entities": []
}
```

| Field | Default | Description |
|-------|---------|-------------|
| `bgm` | — | Playlist id → `audio/bgm/{bgm}.json` |
| `bgmPlaylist` | — | Explicit playlist asset path (overrides `bgm`) |
| `fadeInSeconds` | `1.0` | BGM crossfade in on scene enter |
| `fadeOutSeconds` | `1.0` | BGM fade out on scene exit |
| `pauseBgmOnPause` | `false` | Pause BGM when this scene is paused on the stack |

Playlist JSON lives under `assets/audio/bgm/`. Clip ids and action sounds come from `audio/profile.json` (see [audio.md](audio.md)).

### Audio components

**AmbientSource** — 3D positional loop; requires `Transform`.

| Property | Default | Description |
|----------|---------|-------------|
| `clip` | required | Asset path or profile clip id |
| `clipIsId` | `false` | Resolve `clip` via profile |
| `bus` | `"ambient"` | `"sfx"` or `"ambient"` |
| `volume` | `1` | Base volume 0..1 |
| `loop` | `true` | Loop playback |
| `minDistance` | `1` | Full volume inside |
| `maxDistance` | `50` | Silent beyond |
| `refDistance` | `1` | Reference distance |

```json
"AmbientSource": {
  "clip": "fire_loop",
  "clipIsId": true,
  "minDistance": 2,
  "maxDistance": 25,
  "volume": 0.8
}
```

**SoundEmitter** — one-shot or looped SFX.

| Property | Default | Description |
|----------|---------|-------------|
| `clip` | required | Asset path or profile clip id |
| `clipIsId` | `false` | |
| `bus` | `"sfx"` | `"sfx"` or `"ambient"` |
| `volume` | `1` | |
| `pitch` | `1` | |
| `loop` | `false` | |
| `playOn` | `"manual"` | `"spawn"`, `"interval"`, or `"manual"` |
| `intervalSeconds` | `0` | For `"interval"` |

**FootstepEmitter** — footstep clips from movement; requires `Transform`.

| Property | Default | Description |
|----------|---------|-------------|
| `clips` | required | Array of paths or clip ids |
| `clipIsId` | `false` | |
| `intervalSeconds` | `0.35` | Min time between steps |
| `minSpeed` | `0.5` | World units/sec to emit |
| `bus` | `"sfx"` | |
| `volume` | `0.6` | |

```json
"FootstepEmitter": {
  "clips": ["footstep"],
  "clipIsId": true,
  "intervalSeconds": 0.4,
  "minSpeed": 1.0
}
```

## Custom components

Register types in `onCreate(HermesEngine engine)` via `engine.registry().register(...)`, and add matching logic with
`engine.addSystem(...)`. Alternatively, provide a `META-INF/services/dev.hermes.api.ecs.ComponentRegistration`
implementation that registers both the component deserializer and any systems.

Unknown component type names fail at load time with a message naming the scene file, entity, and type.

## Entity types and template merge

Reusable templates live at `assets/entities/<kind>/type.json`. Reference them from scene JSON:

```json
{
  "type": "spin-cube",
  "id": "cube",
  "components": {
    "Transform": { "x": 3 }
  }
}
```

Merge order: type template `components` → scene `components` (instance wins) → `$ref` resolve → deserialize.
Property wiring between sibling components uses `"$ref": "Transform.x"` (v1: `Transform.x|y|z` only).

Full rules, `spawn()`, and complexity tiers: [entity-types.md](entity-types.md).
