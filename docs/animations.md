# Animations and drawables

Hermes replaces single `Mesh` / `Sprite` components with **`Drawables`** (multi-part visuals) and **`AnimationController`** (clip playback). Two animation sources ship in v1:

| Source | Use for | Author workflow |
|--------|---------|-----------------|
| **Hermes JSON** | 2D sprite cycles, prop motion, UI pulse, material tweens, multi-part offsets | Write `assets/animations/*.json` |
| **glTF / GLB** | Skeletal/skinned **3D** characters and props | Export from Blender/Maya/Mixamo → `assets/models/` |

Runtime control is available through **`AnimationService`** on `HermesEngine`. Custom backends and track targets plug in via SPI.

**Platform policy:** Desktop-first development, but **HTML (TeaVM) parity is a merge gate** — same animation JSON, same glTF assets (split `.gltf` where required), and `:hermes-launcher-html:compileJava` green before merge.

See also: [scene-format-v1.md](scene-format-v1.md) (component tables), [entity-types.md](entity-types.md) (templates), [resource-management.md](resource-management.md) (HTML asset rules), [ARCHITECTURE.md](ARCHITECTURE.md) (system layout).

---

## Author complexity tiers

| Tier | Author writes | Engine does |
|------|---------------|-------------|
| **0 — Single drawable** | `"Drawables": { "sprite": "logo.png" }` or `{ "mesh": "models/cube.obj" }` | Expands to one default part; same as the old single mesh/sprite |
| **1 — Multi-part** | `Drawables.parts[]` with `id`, `kind`, `model`/`texture`, `local` | Composes root `Transform` × part `local` at draw time |
| **2 — Hermes clip** | `AnimationController` + `assets/animations/walk.json` | Keyframes on Transform, part locals, sprite frames, uniforms — **primary 2D path** |
| **2b — glTF clip** | Export `.glb`/`.gltf` from Blender; `{ "type": "gltf", "clip": "Walk" }` | Skeletal 3D; named clips from DCC export |
| **3 — Java control** | `engine.animation().play(entity.id(), "attack")` | Switch clips; query `finished()` / `timeSeconds()` |
| **4 — Custom / SPI** | `AnimationRegistration` new backend or `AnimationTrackResolver` | Future formats, procedural IK, gameplay tracks |

**v1 limits:** Only **Hermes JSON** and **glTF/GLB** animation backends. No clip cross-fade. No JSON state machine graph. Colliders follow entity root `Transform` only — animated part offsets do not move colliders.

---

## Drawables

Every entity with `Drawables` **must** also include a `Material` component (entity-level or per-part override). Scenes without it fail at load time with `SceneParseException`.

### Tier 0 — shorthand

```json
"Drawables": { "sprite": "hermes-logo.png" }
```

```json
"Drawables": { "mesh": "models/cube.obj", "texture": "brick.png" }
```

Shorthand normalizes to a single part with `id: "default"`.

### Tier 1 — multi-part

```json
"Drawables": {
  "parts": [
    {
      "id": "body",
      "kind": "sprite",
      "texture": "sprites/hero-sheet.png",
      "sheet": { "columns": 4, "rows": 1, "frameWidth": 32, "frameHeight": 48 },
      "local": { "y": 0 }
    },
    {
      "id": "shadow",
      "kind": "sprite",
      "texture": "sprites/shadow.png",
      "local": { "y": -0.1, "scaleX": 1.2, "scaleY": 0.4 }
    }
  ]
}
```

| Part field | Applies to | Description |
|------------|------------|-------------|
| `id` | all | Stable part name; referenced by animation tracks (`parts.<id>.*`) |
| `kind` | all | `"mesh"` or `"sprite"` |
| `model` | mesh | OBJ, procedural generator JSON, or glTF path |
| `texture` | sprite / mesh | Albedo texture path |
| `primitive` | mesh | Inline procedural: `"box"`, `"plane"`, or `"sphere"` |
| `size` | mesh + `primitive` | Float array — box `[w,h,d]`, plane `[w,h]`, sphere `[radius, segments]` |
| `local` | all | Part offset from entity root (see below) |
| `sheet` | sprite | Grid metadata for sprite-sheet frames |
| `material` | all | Optional per-part shader/uniform override |
| `rig` | mesh | `"gltf"` for skinned glTF parts; omit for static meshes |

**Pose composition:** `worldPart = root Transform × part local`. Physics, picking, and audio follow the entity root only.

#### Part `local` properties

| Property | Default | Description |
|----------|---------|-------------|
| `x`, `y`, `z` | `0` | Offset from entity root |
| `rotationX`, `rotationY`, `rotationZ` | `0` | Euler rotation in degrees |
| `scaleX`, `scaleY`, `scaleZ` | `1` | Part scale |
| `visible` | `true` | Skip draw when `false` |
| `spriteFrame` | `0` | Current frame index (also writable by Hermes tracks) |

#### Sprite sheet (`sheet`)

| Property | Default | Description |
|----------|---------|-------------|
| `columns` | `1` | Frames per row |
| `rows` | `1` | Row count |
| `frameWidth` | `1` | Pixel width of one cell |
| `frameHeight` | `1` | Pixel height of one cell |

Animate frames with a Hermes track on `parts.<id>.frame` (see below).

#### Procedural mesh generators

Inline on a mesh part:

```json
{
  "id": "floor",
  "kind": "mesh",
  "primitive": "box",
  "size": [20, 0.2, 20],
  "material": { "shader": "default/lit" }
}
```

Or reference a reusable generator asset at `assets/models/primitives/floor.json`:

```json
{
  "version": 1,
  "generator": "box",
  "width": 20,
  "height": 0.2,
  "depth": 20
}
```

Use `"model": "models/primitives/floor.json"` on the part (loader detects the `"generator"` key).

| Generator | Parameters | Default |
|-----------|------------|---------|
| `box` | `width`, `height`, `depth` | `1` each |
| `plane` | `width`, `height` | `1` each (XZ plane, Y-up) |
| `sphere` | `radius`, `segments` | `0.5`, `16` |

---

## AnimationController

Maps logical clip names to Hermes JSON paths or glTF animation names.

```json
"AnimationController": {
  "rigPart": "body",
  "clips": {
    "idle": "animations/hero-idle.json",
    "walk": "animations/hero-walk.json",
    "run": { "type": "gltf", "clip": "Run" }
  },
  "default": "idle",
  "speed": 1.0,
  "autoPlay": true
}
```

| Field | Default | Description |
|-------|---------|-------------|
| `clips` | required | Logical name → Hermes path string **or** clip object (see below) |
| `rigPart` | — | Part id with `"rig": "gltf"`; **required** when any clip has `"type": "gltf"` |
| `default` | first clip key | Plays on spawn when `autoPlay` is true |
| `speed` | `1.0` | Global playback multiplier (stacks with per-clip `speed`) |
| `autoPlay` | `true` | Start `default` clip at entity creation |

Each clip entry is either a **string** (Hermes JSON path) or an **object**:

```json
"walk": "animations/hero-walk.json",
"run": { "type": "gltf", "clip": "Run", "loop": true, "speed": 1.2 }
```

| Clip object field | Default | Description |
|-------------------|---------|-------------|
| `type` | `hermes` for string paths; else `gltf` | v1: `hermes` \| `gltf` only |
| `path` | — | Hermes clip JSON path (when `type` is `hermes`) |
| `clip` | — | glTF animation name (when `type` is `gltf`) |
| `loop` | `true` | Override loop for this logical clip |
| `speed` | controller `speed` | Per-clip multiplier |

Runtime fields (Java only, not JSON): `currentClip`, `activeRef`, `timeSeconds`, `playing`, `finished`.

**v1 playback rule:** One active clip per entity per frame. To combine glTF body motion with Hermes shadow bob, use two entities or switch clips — dual-backend composite playback is v2.

---

## Hermes keyframe clips

Clips live at `assets/animations/<name>.json` (any path; referenced by the controller).

```json
{
  "version": 1,
  "duration": 0.8,
  "loop": true,
  "tracks": [
    {
      "target": "parts.body.local.rotationZ",
      "interpolation": "linear",
      "keyframes": [
        { "t": 0.0, "v": -10 },
        { "t": 0.4, "v": 10 },
        { "t": 0.8, "v": -10 }
      ]
    },
    {
      "target": "parts.hero.frame",
      "interpolation": "step",
      "keyframes": [
        { "t": 0.0, "v": 0 },
        { "t": 0.2, "v": 1 },
        { "t": 0.4, "v": 2 },
        { "t": 0.6, "v": 3 }
      ]
    },
    {
      "target": "Transform.scaleX",
      "interpolation": "linear",
      "keyframes": [
        { "t": 0.0, "v": 1.0 },
        { "t": 0.5, "v": 1.15 },
        { "t": 1.0, "v": 1.0 }
      ]
    },
    {
      "target": "Material.uniforms.u_glow",
      "interpolation": "linear",
      "keyframes": [
        { "t": 0.0, "v": [0, 0, 0, 1] },
        { "t": 0.5, "v": [1, 0.5, 0, 1] },
        { "t": 1.0, "v": [0, 0, 0, 1] }
      ]
    }
  ]
}
```

| Clip field | Required | Description |
|------------|----------|-------------|
| `version` | Yes | Must be `1` |
| `duration` | Yes | Clip length in seconds |
| `loop` | Yes | Repeat when playback reaches `duration` |
| `tracks` | Yes | Array of track objects |

| Track field | Required | Description |
|-------------|----------|-------------|
| `target` | Yes | Property path (grammar below) |
| `interpolation` | Yes | `"step"` (hold) or `"linear"` (numeric lerp; arrays lerp per element) |
| `keyframes` | Yes | `{ "t": seconds, "v": number \| float[] }` |

### Track target grammar (v1)

| Target pattern | Writes to |
|----------------|-----------|
| `Transform.x` … `Transform.scaleZ` | Entity root `Transform` |
| `parts.<id>.local.x` … `parts.<id>.local.scaleZ` | Part `LocalTransform` |
| `parts.<id>.frame` | Part `spriteFrame` (int) |
| `parts.<id>.visible` | Part visibility (bool; keyframe `v` 0/1) |
| `Material.uniforms.<name>` | Entity `Material` uniform (float[]; length must match existing uniform) |

Unknown part id or path → load failure at clip parse time.

Rotations use linear degrees (no quaternion slerp in v1 — sufficient for props and 2D).

Clips load through `ResourceKind.ANIMATION_CLIP` on `ResourceService`. Preload them in scene bundles with `"kind": "animation_clip"`.

---

## glTF skeletal animation (3D)

For skinned characters, mark the mesh part with `"rig": "gltf"` and reference glTF clip names in `AnimationController`:

```json
"Drawables": {
  "parts": [
    {
      "id": "body",
      "kind": "mesh",
      "model": "models/hero.gltf",
      "rig": "gltf"
    }
  ]
},
"AnimationController": {
  "rigPart": "body",
  "clips": {
    "idle": { "type": "gltf", "clip": "Idle" },
    "walk": { "type": "gltf", "clip": "Walk" },
    "attack": { "type": "gltf", "clip": "Attack", "loop": false }
  },
  "default": "idle"
}
```

**Not supported at runtime:** raw `.fbx`, `.dae`, `.blend`. Export to glTF or animate with Hermes JSON.

### Blender → glTF export

1. **File → Export → glTF 2.0 (.glb/.gltf)**
2. Enable **Animation** — each action or NLA strip becomes a named clip in the exported file.
3. Match clip names to `AnimationController.clips` entries (`"clip": "Walk"` must match the glTF animation name).
4. Use **`hero.glb`** on desktop/Android for convenience, or **split `.gltf` + `.bin` + PNG textures** for cross-platform (required for HTML).

Skinned draws use the same `default/lit` forward-lit path as static meshes — not stock PBR shaders.

---

## HTML asset rules (split glTF)

Hermes HTML builds embed the entire assets tree at compile time. Animated 3D on web requires **split glTF** — not monolithic `.glb` with embedded buffers.

| Topic | Desktop / Android | HTML (TeaVM) |
|-------|-------------------|--------------|
| Hermes JSON clips | Yes | Yes — pure Java, no platform deps |
| `.glb` single file | Yes | **Avoid** — embedded buffers hit libGDX Pixmap/TeaVM limits |
| Split `.gltf` + `.bin` + PNG | Yes | **Yes — merge-gate format** |
| Skinning shader | `default/lit` + gdx-gltf | Same — must pass `hermesDoctor` html-custom-shaders check |

**Authoring checklist for HTML targets:**

1. Export **split glTF** (`.gltf` JSON + external `.bin` + PNG textures).
2. Reference the `.gltf` path in `Drawables.parts[].model` — one scene JSON works on desktop and web.
3. Do **not** put `.glb` paths in `resources/bundles/*.json` when HTML is enabled — `hermesDoctor` reports an **error**.
4. Verify `:hermes-launcher-html:compileJava` and browser smoke on the animation scene before merge.

Full HTML resource table (cooperative async, bundle doctor checks): [resource-management.md — HTML / TeaVM](resource-management.md#html--teavm).

---

## Java API — `AnimationService`

`HermesEngine.animation()` controls playback on the **active scene's** entity store:

```java
AnimationService anim = engine.animation();

anim.play(entity.id(), "walk");
anim.play(entity.id(), "attack", true);  // restart even if already playing
anim.stop(entity.id());
anim.setSpeed(entity.id(), 1.5f);

String clip = anim.currentClip(entity.id());
float t = anim.timeSeconds(entity.id());
boolean playing = anim.isPlaying(entity.id());
boolean done = anim.isFinished(entity.id());
```

Typical gameplay pattern — switch clips on input:

```java
if (engine.input().justPressed("attack")) {
    engine.animation().play(player.id(), "attack", true);
}
```

Query `AnimationController` directly from `EntityStore` when you need config (clip map, `rigPart`) without changing playback.

---

## SPI extension points (tier 4)

Register via `META-INF/services/`:

### `AnimationRegistration`

```java
public final class MyAnimationRegistration implements AnimationRegistration {
    @Override
    public void register(HermesEngine engine, AnimationRegistrar registrar) {
        registrar.backend(new MyAnimationBackend());       // core AnimationBackend impl
        registrar.trackResolver(new MyTrackResolver());    // custom target paths
    }
}
```

Service file: `META-INF/services/dev.hermes.api.animation.AnimationRegistration`

| Registrar method | Purpose |
|------------------|---------|
| `backend(Object)` | Register an `AnimationBackend` for a new `AnimationClipType` (Spine, atlas sequences, etc.) |
| `trackResolver(AnimationTrackResolver)` | Handle custom Hermes track target strings |

Built-in v1 backends: **`HermesTrackBackend`** (JSON keyframes) and **`GltfAnimationBackend`** (skeletal glTF). Both register on `AnimationBackendRegistry`; `AnimationSystem` delegates to `registry.require(ref.type())`.

### `AnimationTrackResolver`

```java
public interface AnimationTrackResolver {
    /** @return true when handled, false to fall through to built-in targets */
    boolean apply(String target, float value, float[] valueArray,
                  EntityId entityId, EntityStore entities);
}
```

Use for gameplay-driven properties (e.g. `"gameplay.healthBar"`) without forking the clip loader.

---

## Example scenes and templates

| Asset | What it demonstrates |
|-------|---------------------|
| `dogfood-simulation/.../scenes/animation-starter.json` | Hermes pulse logo + glTF character + sprite-sheet walker |
| `entities/spin-cube/type.json` | Multi-part mesh + sprite, Hermes pulse clip |
| `entities/gltf-character/type.json` | glTF rig + idle/walk clips |
| `entities/walker/type.json` | Sprite sheet + Hermes frame track |
| `animations/logo-pulse.json` | Root `Transform` scale pulse |

---

## Related docs

- [Scene format v1](scene-format-v1.md) — `Drawables` and `AnimationController` component tables
- [Entity types](entity-types.md) — template merge and `$ref`
- [Resource management](resource-management.md) — bundles, catalog, HTML cooperative loading
- [Render pipeline](render-pipeline.md) — `world3d` and `sprites` passes draw drawable parts
- [Input](input.md) — tier-3 clip switching from gameplay actions
