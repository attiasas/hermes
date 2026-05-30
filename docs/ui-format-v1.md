# UI format v1

Hermes screen-space and world-attached overlays use **widget trees** in `assets/ui/*.json`. The `UiService` loads documents, lays them out in **design pixels**, scales to the backbuffer **SURFACE**, resolves **bindings**, and draws them in the render pipeline `ui` pass.

Hermes is **pre-release**; UI JSON stays **v1** until 1.0. Update this doc in the same PR when loader rules change.

## Where UI is declared

| Mechanism | Config | Use |
|-----------|--------|-----|
| Scene `"ui"` | Top-level field on scene JSON | Full-screen HUD, menus, pause overlays |
| `UiAttach` | Component on a scene entity | HP bars, nameplates following a world entity |
| Programmatic | `engine.ui().load(...)`, `setBinding`, SPI (tier 4) | Custom trees, dynamic values, custom widget types |

See [scene-format-v1.md](scene-format-v1.md) for scene `"ui"` and `UiAttach`. See [render-pipeline.md](render-pipeline.md) for the `ui` pass. See [input.md](input.md) for button → action wiring.

## Author complexity tiers

| Tier | You write | Engine does |
|------|-----------|-------------|
| **0 — Static UI** | `assets/ui/*.json` + scene `"ui": "ui/menu.json"` + `inputContext` + actions in `input/profile.json` | Load tree, layout, draw, route button `action` to `InputService` |
| **1 — Scene stack modals** | `render/ui-overlay.json` pipeline + `push("pause")` + overlay scene `"ui"` | Transparent clear + stacked UI; input context from top scene |
| **2 — Dynamic values** | Widget `"binding": "player.hp"` + `engine.ui().setBinding(...)` or `UiBindingProvider` | Resolve bindings each frame before layout/draw |
| **3 — World-attached** | `UiAttach` entity + `ui/hp-bar.json` + `"follow": "player"` | `UiAttachSystem` projects world position → screen anchor |
| **4 — Custom widgets** | `UiWidgetRegistration` SPI + optional programmatic `UiNode` edits | Custom types in registry (v1 registry stub; SPI loads with engine) |

**v1 limit:** Fully dynamic HUD/score with **zero** Java requires `setBinding` from gameplay code (or a `UiBindingProvider`). Declarative ECS/component bindings are planned for v2.

## Document shape

Path: `assets/ui/<name>.json` (referenced from scene `"ui"` or `UiAttach.document`).

```json
{
  "version": 1,
  "designSize": { "width": 1280, "height": 720 },
  "root": {
    "type": "panel",
    "id": "root",
    "layout": { "anchor": "stretch" },
    "children": [
      {
        "type": "label",
        "id": "title",
        "text": "Hermes",
        "layout": { "anchor": "topCenter", "offsetY": -48, "width": 400, "height": 64 },
        "style": { "color": [1, 1, 1, 1] }
      },
      {
        "type": "button",
        "id": "play",
        "text": "Play",
        "action": "start_game",
        "layout": { "anchor": "center", "width": 220, "height": 56 }
      }
    ]
  }
}
```

### Top-level fields

| Field | Required | Description |
|-------|----------|-------------|
| `version` | Yes | Must be `1`. |
| `designSize` | Yes | `width` / `height` in **design pixels** (authoring resolution). Default mental model: `1280×720`. |
| `root` | Yes | Root `UiNode` object. |

Invalid or unknown widget `type` values fail at load time with `UiDocumentParseException`.

## Nodes

Every node is a JSON object.

| Field | Required | Description |
|-------|----------|-------------|
| `type` | Yes | Built-in: `panel`, `image`, `label`, `button`, `progressBar`, `spacer`. Custom types via SPI (tier 4). |
| `id` | No | Stable id for layout bounds and hit-testing. **Buttons need a non-empty `id` to receive clicks.** |
| `layout` | No | Anchor, offsets, size, padding, `zIndex` (see below). |
| `style` | No | Map passed to renderer (primarily `color` RGBA array). |
| `children` | No | Array of child nodes. Draw order: pre-order tree walk; higher `layout.zIndex` draws later among siblings. |
| *(other)* | — | Type-specific props (`text`, `action`, `binding`, `texture`, …) at the same level as `type`. |

**Anti-pattern:** Do not create one ECS entity per label or button. One menu = one document tree. `UiAttach` entities are anchors only (no `Sprite`).

### Layout

All offsets and sizes are in **design pixels** before scene `fitMode` scales the tree to SURFACE.

| Property | Default | Description |
|----------|---------|-------------|
| `anchor` | `topLeft` | Placement within parent rect (see anchors table). |
| `offsetX`, `offsetY` | `0` | Offset from anchor point in design pixels. |
| `width`, `height` | *(unset)* | Explicit size. `0` or omitted with no intrinsic measure → use parent content area / defaults. |
| `paddingLeft`, `paddingTop`, `paddingRight`, `paddingBottom` | `0` | Insets inside parent for child layout. |
| `zIndex` | `0` | Higher values draw later (hit-test walks children back-to-front). |

#### Anchors

| `anchor` | Placement |
|----------|-----------|
| `topLeft`, `topCenter`, `topRight` | Top edge |
| `centerLeft`, `center`, `centerRight` | Vertical middle |
| `bottomLeft`, `bottomCenter`, `bottomRight` | Bottom edge |
| `stretch` | Fill parent (typical for root `panel`) |

### Scene fit modes

When a scene references a UI document, `SceneUiConfig.fitMode` scales design → backbuffer:

| `fitMode` | Behavior |
|-----------|----------|
| `fit` | Uniform scale (letterbox-style; default) |
| `stretch` | Non-uniform scale to fill surface |
| `fill` | Uniform scale to cover surface (may crop) |

Optional `designAspect` on scene `"ui"` object overrides aspect used with `fit`. Shorthand `"ui": "ui/menu.json"` uses `fit` and aspect from `designSize`.

`UiService` uses an internal orthographic projection from `designSize` + `fitMode` — **no** `ui-camera` entity in scenes.

## Built-in widget types

### `panel`

Container; draws a background when configured.

| Prop | Description |
|------|-------------|
| `texture` or `src` | Background texture path (assets root). |
| `nineSlice` | When `true`, stretch with `sliceLeft`, `sliceRight`, `sliceTop`, `sliceBottom` (default 8). |
| `style.color` | RGBA `[r,g,b,a]` fill when no texture (default dark gray). |

### `image`

| Prop | Description |
|------|-------------|
| `texture` or `src` | Required for visible draw. |

### `label`

| Prop | Description |
|------|-------------|
| `text` | String to draw. |
| `font` | Bitmap font asset path (e.g. `fonts/default.fnt`); default engine font when omitted. |
| `style.color` | Text color RGBA. |

### `button`

| Prop | Description |
|------|-------------|
| `text` | Centered label. |
| `action` | Input action name; on click, `InputService.pulseAction(action)` (see [input.md](input.md)). |
| `texture` / `src`, `font`, `style.color` | Visuals (default blue rect if no texture). |

Bind the same action in `input/profile.json` for keyboard/gamepad if needed.

### `progressBar`

| Prop | Description |
|------|-------------|
| `binding` | Dot-key string **or** literal `value` number when not binding. |
| `maxBinding` | Dot-key for max **or** literal `max` (default max `100`). |
| `texture` / `style` | v1 draws solid fill from binding fraction; texture props reserved for future styling. |

Fill = `value / max` clamped to `0..1`. Missing or non-numeric binding → `0` for value, default max for `maxBinding`.

### `spacer`

Layout-only; no draw ops. Use for flexible gaps in a `panel` tree.

## Data bindings (v1)

Bindings are **dot-keys** (e.g. `player.hp`, `load.progress`).

**Resolution order** (first hit wins):

1. `engine.ui().setBinding(key, value)` — gameplay / `onCreate`
2. Registered `UiBindingProvider.resolve(key)` callbacks
3. *(v2)* Declarative ECS/component bindings

`RuntimeConfigService` is **not** used for per-frame HUD state — only launch-time Hermes settings ([runtime-config.md](runtime-config.md)).

| Value type | Widget use |
|------------|------------|
| `Number` | `progressBar` value/max |
| `String` / `Boolean` | Planned for labels/toggles (v2); set via bindings for custom widgets |

Example (tier 2):

```java
engine.ui().setBinding("player.hp", health.current);
engine.ui().setBinding("player.hpMax", health.max);
```

```json
{
  "type": "progressBar",
  "id": "hp",
  "binding": "player.hp",
  "maxBinding": "player.hpMax",
  "layout": { "anchor": "bottomLeft", "width": 200, "height": 12 }
}
```

## Actions and input

Buttons do not call Java directly. They pulse a named **action** into the same namespace as `input/profile.json`:

```json
{ "type": "button", "id": "play", "text": "Play", "action": "start_game" }
```

```json
{
  "bindings": [
    { "action": "start_game", "source": "keyboard", "key": "ENTER" },
    { "action": "start_game", "source": "pointer", "button": "LEFT", "when": "justPressed" }
  ]
}
```

`UiInputSystem` (GLOBAL) hit-tests the **active** (top) scene tree only: pointer SCREEN → `mapScreenToSurface` → deepest `button` by layout bounds. Gameplay entity picking stays `Selectable` + `PickLayer.WORLD` — see [input.md](input.md).

Set scene `"inputContext": "menu"` while a menu scene is active so context-specific bindings apply.

## World-attached UI (`UiAttach`)

Component on a lightweight marker entity (no `Sprite` / `RenderLayer` required):

```json
{
  "id": "player-hp",
  "components": {
    "UiAttach": {
      "document": "ui/hp-bar.json",
      "follow": "player",
      "offsetX": 0,
      "offsetY": 2.2,
      "offsetZ": 0,
      "visible": true
    }
  }
}
```

| Property | Default | Description |
|----------|---------|-------------|
| `document` | — | UI asset path (required). |
| `follow` | — | Target entity **`id`** in the same scene (`EntityStore.findByName`). |
| `offsetX`, `offsetY`, `offsetZ` | `0` | World-space offset applied before `worldToScreen`. |
| `visible` | `true` | When `false`, attach is skipped. |

`UiAttachSystem` projects the follow target each frame; the attach document is laid out with a screen anchor at that point.

## Coordinates

| Space | UI role |
|-------|---------|
| **NORMALIZED** | Anchor math on full backbuffer rect (via `SceneViewport.normalizedToSurface`). |
| **SURFACE** | Layout output, draw, and hit-test rectangles (physical pixels). |
| **WORLD** | `UiAttach` reads `Transform`, projects with `ViewportService.worldToScreen`. |
| **SCREEN** | Raw pointer; convert before hit-test. |

Details: [coordinate-spaces.md](coordinate-spaces.md).

## Extension (tier 4)

Register custom widget types with Java SPI:

```text
META-INF/services/dev.hermes.api.ui.UiWidgetRegistration
```

Implement `UiWidgetRegistration` and register on `engine.ui().widgets()` from `HermesEngineImpl` bootstrap (same pattern as `ComponentRegistration`).

Programmatic trees: `engine.ui().load("ui/hud.json")` returns `UiDocument`; mutate `UiNode` children in `onCreate` when needed.

## Breaking changes (sprite UI removed)

| Removed | Use instead |
|---------|-------------|
| `UiPass` + `Sprite` + `RenderLayer.UI` | `UiRenderPass` + widget trees |
| `RenderLayer` value `"UI"` | `WORLD` only; UI via scene `"ui"` |
| `PickLayer.UI`, `PickLayer.ANY` | `WORLD` only; UI via widget hit-test |
| `Selectable.layer: "UI"` for menus | Button widgets + `UiInputSystem` |
| `ui-camera` entities | `UiService` internal ortho from `designSize` + `fitMode` |
| Pipeline `ui` pass `"layers": ["UI"]`, `"camera": "ui-camera"` | `{ "id": "ui", "type": "ui", "target": "screen", "depthTest": false }` |

Migrate pause/menu scenes: drop UI camera and HUD sprites; add `"ui": "ui/....json"` and update pipelines.

## Related docs

- [Scene format v1](scene-format-v1.md) — `"ui"`, `UiAttach`
- [Render pipeline](render-pipeline.md) — `ui` pass
- [Input system](input.md) — actions, picking
- [Scene management](scene-management.md) — stack overlays
- [Architecture](ARCHITECTURE.md) — `UiService` module boundary
