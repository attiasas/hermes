# Input system

Hermes routes hardware input through a single stack: device polling → remapped **actions** (from JSON) → **viewport-aware**
screen/world coordinates → optional **world picking**. Games read `engine.input()` on `HermesEngine`; coordinate math lives
in `engine.viewport()` (see [coordinate-spaces.md](coordinate-spaces.md)).

## Profile path

Every game declares an input profile in `hermes.json`:

```json
{
  "title": "MyGame",
  "scene": "scenes/main.json",
  "renderPipeline": "render/pipeline.json",
  "inputProfile": "input/profile.json"
}
```

The path is relative to the assets root (`hermes.assetsDirectory`, default `src/main/resources/assets/`). The launcher
loads the profile at startup; a missing file fails fast. Schema details: [input-format-v1.md](input-format-v1.md).

## InputService

`HermesEngine.input()` returns `InputService`:

| API | Role |
|-----|------|
| `poll(deltaSeconds)` | Called once per frame by the launcher before systems run. |
| `actions()` | Remapped buttons/axes from the active profile context (`InputActions`). |
| `devices()` | Raw keyboard, pointer, and gamepad snapshots for the current frame (`InputDevices`). |
| `viewport(entities)` | Delegates to `engine.viewport().forWorld(entities)`. |
| `pick(entities, screenX, screenY)` | Screen-space hit test against `Selectable` entities (default `PickLayer.WORLD`). |
| `pick(entities, screenX, screenY, layer)` | Same, filtered by `PickLayer` (`WORLD` only). |

### Actions vs devices

- **Actions** — Named in `input/profile.json` (`select`, `pause`, `move_x`, …). Use `input.actions().pressed("select")`,
  `justPressed`, `axis`, etc. Bindings can vary by **context** (profile default or scene `inputContext` override).
- **Devices** — Direct libGDX-aligned access when you do not want remapping: `input.devices().pointer().screenX()`,
  `keyboard().justPressed(InputKey.F1)`, `gamepad(0).axis(GamepadAxis.LEFT_X)`.

Prefer actions for gameplay controls shared across keyboard, pointer, and gamepad. Use devices for debug shortcuts or
custom pointer logic.

### Coordinate spaces

Pointer hardware reports **SCREEN** coordinates (window bottom-left). Picking and `screenToWorld` go through
`ViewportService` so letterboxing and FBO targets match rendering. See [coordinate-spaces.md](coordinate-spaces.md).

## Built-in scene interaction

`BuiltinComponents` registers three **GLOBAL** systems (no game Java required for stock demos):

| System | When it runs | Behavior |
|--------|----------------|----------|
| `SelectionSystem` | `actions.justPressed("select")` | Picks at pointer; sets `Selected` on hit, clears selection on miss. |
| `CameraSceneControlSystem` | Active camera is **perspective** | Empty left-drag orbits camera around `lookAt` (or origin). |
| `EntityDragSystem` | Active camera is **orthographic** | Left-drag moves the entity with `Selected`. |

Stock scenes in [dogfood-simulation](../dogfood-simulation/) and [hermes-templates](../hermes-templates/) ship
`Selectable` entities and `input/profile.json` with pointer → `select`. See [scene-management.md](scene-management.md).

## Cookbook

### Click to select (config-only)

1. Bind pointer left click to a button action in `input/profile.json`:

```json
{ "action": "select", "source": "pointer", "button": "LEFT", "when": "justPressed" }
```

2. Tag pickable entities in scene JSON:

```json
"Selectable": { "radius": 48, "layer": "WORLD" }
```

`SelectionSystem` handles the rest. Optional `Selected` marker is added at runtime (not required in JSON).

### 3D orbit camera on empty drag

Use a **perspective** active camera (dogfood `main.json`, `minimal` / `multi-scene` templates). Drag on empty space
starts an orbit; clicking a `Selectable` entity selects it instead. Set `Camera.lookAt` for a stable orbit target:

```json
"Camera": {
  "projection": "perspective",
  "active": true,
  "lookAt": { "x": 0, "y": 0, "z": 0 }
}
```

### 2D drag selected entity

Use an **orthographic** camera (`hermes-templates/2d`). Two sprites with `Selectable`; click to select, drag to move in
world XY. `EntityDragSystem` maps pointer deltas through `viewport.screenToWorld` at the entity’s `Transform.z`.

### Move toward clicked world point

Custom movement when the built-in drag system is not enough:

```java
InputService input = engine.input();
EntityStore entities = engine.scenes().activeManager().entities();
if (input.actions().justPressed("select")) {
  PointerSnapshot p = input.devices().pointer();
  Vec3 target = new Vec3();
  engine.viewport().screenToWorld(entities, p.screenX(), p.screenY(), 0f, target);
  // Steer entity toward target.x / target.y (or use PickHit from pick())
}
```

Or use `input.pick(entities, p.screenX(), p.screenY())` when you only care about hits on `Selectable` entities; `PickHit`
includes `worldX`, `worldY`, `worldZ`.

### Pick layer

`Selectable.layer` must be `"WORLD"`. Screen-space UI uses widget hit-testing (`UiInputSystem`), not `pick(...)`.

### UI buttons and actions

Menu and HUD buttons live in `assets/ui/*.json` with an `"action"` string per button. `UiInputSystem` hit-tests the **active**
scene tree (top of stack) and calls `input.pulseAction(action)` on click. Map the same action names in `input/profile.json`
for keyboard, gamepad, or pointer so gameplay systems can use `input.actions().wasActionPressed("start_game")`.

Example: pause overlay scene sets `"ui": "ui/pause-menu.json"` and `"inputContext": "menu"` while gameplay uses
`"inputContext": "gameplay"` on the main scene. Full widget and binding rules: [ui-format-v1.md](ui-format-v1.md).

## Scene context override

Scene JSON may set a top-level `inputContext` (see [scene-format-v1.md](scene-format-v1.md)). When that scene is active,
`actions.context()` returns the scene value; bindings with a matching `"context"` field in the profile apply. Bindings
without `context` (or with `"*"`) apply in every context.

## Related docs

- [UI format v1](ui-format-v1.md) — widget trees, bindings, breaking changes
- [Input profile format v1](input-format-v1.md)
- [Scene format v1](scene-format-v1.md) — `Selectable`, `Selected`, `inputContext`, `"ui"`, `UiAttach`
- [Coordinate spaces](coordinate-spaces.md)
- [Scene management](scene-management.md)
- [Architecture](ARCHITECTURE.md)
