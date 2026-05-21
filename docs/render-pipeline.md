# Render pipeline

Hermes draws scenes through a JSON **render pipeline** (`assets/render/pipeline.json` by convention). The path is set in `hermes.json` as `renderPipeline` and may be overridden per scene.

## Pipeline document (version 1)

| Field | Description |
|-------|-------------|
| `version` | Must be `1`. |
| `clearColor` | Optional RGBA array; default `[0, 0, 0, 1]`. |
| `framebuffers` | Off-screen render targets (FBOs). |
| `shaders` | Named shader programs (vertex + fragment asset paths). |
| `passes` | Ordered draw passes. |

### Framebuffers

Each entry defines a pooled framebuffer created at graph build time and resized with the window:

```json
"framebuffers": [
  { "id": "sceneColor", "width": 0, "height": 0, "depth": true },
  { "id": "hud", "width": 320, "height": 180, "depth": false }
]
```

| Property | Description |
|----------|-------------|
| `id` | Unique name referenced by pass `target` or camera `renderTarget`. |
| `width`, `height` | Pixel size; `0` means use the current window width or height. |
| `depth` | When `true`, allocates a depth buffer for 3D passes. |

`FramebufferPool` owns GPU framebuffers, recreates them when dimensions change, and disposes them with the render graph.

### Pass targets

Every pass has a `target`:

- `"screen"` — default backbuffer (window).
- A framebuffer `id` from the `framebuffers` array — the pass renders into that FBO.

Passes that target an unknown framebuffer id fail at graph build time.

### Pass types

| `type` | Description |
|--------|-------------|
| `world3d` | Meshes with materials; perspective/ortho from active `Camera`. |
| `sprites` | `Sprite` entities by `RenderLayer`. |
| `ui` | UI layer sprites; `depthTest` usually `false`. |
| `custom` | Code-registered pass; requires `handler` matching `HermesApplication.configureRendering`. |

### Custom passes

Register handlers in `configureRendering` before the pipeline is built:

```java
@Override
public void configureRendering(HermesRenderConfigurator configurator) {
  configurator.registerPass("water", new WaterPass());
}
```

Pipeline JSON references the handler by id:

```json
{ "id": "water", "type": "custom", "handler": "water", "target": "screen" }
```

Unregistered handlers fail at graph build time. Custom passes receive the active scene `World` each frame (no libGDX types in the API).

Example:

```json
"passes": [
  { "id": "world3d", "type": "world3d", "target": "sceneColor", "layers": ["WORLD"] },
  { "id": "sprites", "type": "sprites", "target": "screen", "layers": ["WORLD"] },
  { "id": "ui", "type": "ui", "target": "screen", "layers": ["UI"], "depthTest": false }
]
```

## Camera render targets

On a `Camera` component, optional `renderTarget` names a pipeline framebuffer id:

```json
"Camera": {
  "projection": "perspective",
  "active": true,
  "renderTarget": "sceneColor"
}
```

The field is parsed and stored on the component for future routing (e.g. binding the active camera’s FBO before world passes). Today, pass `target` in JSON controls where each pass draws.

## Scene overrides

A scene may set `"renderPipeline": "render/ui-overlay.json"` to use a different graph (for example a UI-only pause menu). Resolution order: scene JSON override → `SceneDefinition.renderPipeline` → project default from `hermes.json`.

## UI pass camera

UI passes may pin the scene camera by entity id:

```json
{ "id": "ui", "type": "ui", "target": "screen", "layers": ["UI"], "camera": "ui-camera", "depthTest": false }
```

The entity `ui-camera` must have `Transform` + `Camera` (usually orthographic). When omitted, the pass uses the active scene camera.

## HUD framebuffer (future UI system)

A UI pass may target a dedicated framebuffer (for example `"target": "hud"`) declared under `framebuffers`. A later composite pass can blit `hud` to `screen` for engine-managed HUD layers. Declare `hud` with `depth: false` for pure 2D overlays.

## Future passes (not implemented yet)

| `type` | Planned use |
|--------|-------------|
| `post` | Full-screen post-processing chain (bloom, color grade, etc.) |
| `particles` | World or entity particle emitters |
| `compute` | GPU compute prepass (desktop only; skipped on HTML/TeaVM) |

These types parse successfully in pipeline JSON today. At runtime the engine logs once per pass id and skips drawing until implemented.

## HTML and custom GLSL

TeaVM/HTML supports **builtin** shaders only in v1 (`shaders/default.vert` and `shaders/default.frag`). `hermesDoctor` **fails** when the HTML platform is enabled and `assets/shaders/` contains any other `.vert` / `.frag` files. Use desktop-only custom shaders or disable HTML in `settings.gradle`.

## Builtin default

`assets/render/builtin-forward.json` in `hermes-core` is the canonical forward pipeline (world3d → sprites → ui to screen). Templates copy or reference an equivalent `pipeline.json`.
