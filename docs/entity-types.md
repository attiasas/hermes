# Entity types

Hermes supports **config-first entity templates**: reusable component bundles under `assets/entities/<kind>/type.json`.
Scenes reference a template with `"type"` (preferred) or `"kind"`, optionally override fields in `components`, and share
the same creation pipeline as inline entities and runtime `spawn()`.

Templates are scanned at startup from the game assets root. No Java is required for props, markers, or other
config-driven entities — add systems only when you need cross-entity or per-frame logic.

## Folder layout

```
assets/
  entities/
    spin-cube/
      type.json
    enemy/
      type.json
  scenes/
    main.json
```

| Rule | Detail |
|------|--------|
| Path | `assets/entities/<kind>/type.json` |
| Kind id | **Must match the parent directory name** (`spin-cube/` → kind `"spin-cube"`) |
| Scan | `engine.entityTypes().scanAssets()` runs at launcher startup |
| Manual register | `engine.entityTypes().register(kind, assetPath)` for tests or non-standard paths |

## `type.json` (version 1)

```json
{
  "version": 1,
  "components": {
    "Transform": { "x": 0, "y": 0, "z": 0 },
    "Drawables": {
      "parts": [
        { "id": "cube", "kind": "mesh", "model": "@cube" },
        {
          "id": "logo",
          "kind": "sprite",
          "texture": "@logo",
          "local": { "y": 1.4, "scaleX": 0.25, "scaleY": 0.25 }
        }
      ]
    },
    "Material": { "shader": "default/lit" },
    "AnimationController": {
      "clips": { "pulse": "animations/logo-pulse.json" },
      "default": "pulse"
    },
    "SpinMarker": {
      "speed": 1.2,
      "centerX": { "$ref": "Transform.x" },
      "centerY": { "$ref": "Transform.y" },
      "radius": 1.5
    },
    "Selectable": { "radius": 2.0, "layer": "WORLD" }
  }
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `version` | Yes | Must be `1`. |
| `components` | No | Default component map (same shape as scene `entities[].components`). |

Every entity with `Drawables` **must** also have `Material` after merge (same rule as [scene-format-v1.md](scene-format-v1.md)).

### glTF character template

Skeletal 3D characters use a rigged mesh part and glTF clip names:

```json
{
  "version": 1,
  "components": {
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
    "Material": { "shader": "default/lit" },
    "AnimationController": {
      "rigPart": "body",
      "clips": {
        "idle": { "type": "gltf", "clip": "Idle" },
        "walk": { "type": "gltf", "clip": "Walk" }
      },
      "default": "idle"
    }
  }
}
```

Scene instance:

```json
{
  "type": "gltf-character",
  "id": "hero",
  "components": {
    "Transform": { "x": 0, "y": 0, "z": 0 }
  }
}
```

Export split `.gltf` + `.bin` + PNG for HTML targets. Full animation guide: [animations.md](animations.md).

## Scene entities: `type`, `kind`, and merge

Scene JSON fields for typed entities:

```json
{
  "type": "spin-cube",
  "id": "cube-a",
  "components": {
    "Transform": { "x": 3 }
  }
}
```

`"kind": "spin-cube"` is equivalent to `"type"`. The parser normalizes to one internal kind string.

### Merge rules

Creation always runs through `EntityFactory`:

1. **Template** — if `entityTypes().has(kind)`, load `type.json` `components`; otherwise use an empty template.
2. **Instance** — deep-merge scene `components` on top (per component type; instance wins on conflict; nested objects merge recursively).
3. **`$ref`** — resolve references on the **merged** JSON (see below).
4. **Deserialize** — each component type in stable key order.
5. **Validate** — drawable + `Material` check.

| Source | Template | Instance overrides | Stored `EntityKind` |
|--------|----------|-------------------|---------------------|
| Inline entity (no type/kind) | empty | scene `components` | `UNSET` |
| Typed entity (registered kind) | `type.json` | scene `components` | `EntityKind.of(kind)` |
| Tag-only kind (no `entities/<kind>/`) | empty | scene `components` | kind string stored; **no** template merge |
| `spawn("spin-cube")` | registered template | empty | `EntityKind.of("spin-cube")` |

**Example:** template sets `Transform.x: 0`; scene overrides `"Transform": { "x": 9 }`. A `$ref` to `Transform.x` in
the template resolves to **9**, not 0.

See [scene-format-v1.md](scene-format-v1.md) for the full scene schema.

## `$ref` syntax (v1)

Wire sibling component fields in JSON without Java. A property is a reference only when it is a JSON object with
**exactly one** key `"$ref"` and a string value:

```json
"centerX": { "$ref": "Transform.x" }
```

### Supported paths (v1)

| Path | Resolves to |
|------|-------------|
| `Transform.x` | `float` from merged `Transform.x` (default `0`) |
| `Transform.y` | same for `y` |
| `Transform.z` | same for `z` |

Refs resolve at the **top level** of each component's property object (not inside nested objects or arrays). Unknown
paths or missing source components fail at load with `SceneParseException` (asset path, entity name, ref string).

Refs run **after merge, before deserialize**, so instance overrides are visible to template refs and deserialize order
does not matter.

### `$ref` vs `ComponentContext`

| Mechanism | When | Use for |
|-----------|------|---------|
| `$ref` | After merge, before deserialize | Copy float fields from sibling component JSON |
| `ComponentContext.sibling` | During deserialize | Conditional logic, non-float fields, computed defaults |

Prefer `$ref` in templates; use `ComponentContext` in custom deserializers when JSON refs are not enough.

## Runtime `spawn()`

Spawn creates an entity from a registered template with **no** instance overrides:

```java
WorldManager manager = engine.scenes().activeManager();
EntityStore entities = manager.entities();

Entity enemy = entities.spawn("enemy");           // unnamed
Entity boss = entities.spawn("enemy", "boss-1");  // optional name
```

Requirements:

- Kind must be registered (`entityTypes().has(kind)`).
- `EntityStore` must be wired with an `EntityFactory` (scene-loaded stores are; bare `EntityStoreImpl()` in tests is not).

Spawn uses the same merge → `$ref` → deserialize path as scene loading.

## WorldManager and EntityStore

Each loaded scene owns one **`WorldManager`** — the per-scene simulation root. Today it exposes only the entity store;
future scene services (lighting state, save snapshots, audio buses) will attach here without another rename.

```java
WorldManager manager = engine.scenes().activeManager();
EntityStore entities = manager.entities();

Entity cam = entities.findByName("main-camera");
Collection<Entity> spinners = entities.entitiesWith(SpinMarker.class);
Collection<Entity> enemies = entities.entitiesWithKind(EntityKind.of("enemy"));
```

| Type | Role |
|------|------|
| `WorldManager` | Per-scene root; `entities()` returns the store |
| `EntityStore` | ECS storage: create, spawn, components, queries |
| `EntityTypeRegistry` | Template catalog from `entities/*/type.json`; `engine.entityTypes()` |

Scene stack APIs use `manager()`, not a separate world type:

- `SceneManager.activeManager()` — top scene's `WorldManager`
- `SceneHandle.manager()` / `SceneContext.manager()` — per-scene access in lifecycle and systems

Render and input take **`EntityStore`**: `RenderGraph.render(entities)`, `InputService.pick(entities, …)`,
`ViewportService.forWorld(entities)`.

## Complexity tiers

| Tier | What you write | Example |
|------|----------------|---------|
| **1 — Templates** | `type.json` + scene `"type"` + small overrides | Spinning cube from `entities/spin-cube/type.json` (Drawables + Hermes pulse) |
| **2 — Runtime spawn** | Same templates; `entities.spawn(kind)` | Spawn enemies from a wave system |
| **3 — Animation clips** | `AnimationController` + Hermes JSON or glTF clips in template | Walk cycles, logo pulse — no Java ([animations.md](animations.md)) |
| **4 — Java control** | `engine.animation().play(...)` or custom components | Clip switching, `PulseMarker`-style markers |
| **5 — Systems / SPI** | `System.update(WorldManager, float)` or `AnimationRegistration` | Movement, AI, custom animation backends |

Start at tier 1. Add Java only when config cannot express the behavior.

## Cross-entity logic = systems

Entity templates and `$ref` wire **sibling fields on one entity**. Behavior that reads or writes **other entities**
belongs in **`System` implementations**, not in JSON:

```java
@Override
public void update(WorldManager manager, float deltaSeconds) {
    EntityStore entities = manager.entities();
    for (Entity e : entities.entitiesWithKind(EntityKind.of("enemy"))) {
        // ...
    }
}
```

Register systems in `onCreate` with `SystemScope.ACTIVE_SCENE` for top-scene gameplay or `GLOBAL` for stack-wide logic.
See [scene-management.md](scene-management.md).

## Reserved entity names

Engine-reserved names must not appear in game scene JSON (duplicate `id` already fails at load):

| Name | Purpose |
|------|---------|
| `__hermes_scene__` | Scene-level holder components (e.g. future `SceneLightingState`) |

A post-load hook will ensure reserved entities exist when those features land. Do not author entities with these names.

## Related docs

- [Animations](animations.md) — Hermes clips, glTF, drawables, HTML parity
- [Scene format v1](scene-format-v1.md) — scene JSON schema
- [Scene management](scene-management.md) — stack, `SystemScope`, lifecycle
- [Architecture](ARCHITECTURE.md) — ECS module layout
- [Input system](input.md) — picking against `EntityStore`
