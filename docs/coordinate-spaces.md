# Coordinate spaces

Hermes uses four canonical coordinate spaces. All projection, `glViewport`, and screen ↔ world math flows through
`ViewportService` on `HermesEngine` — render passes and (future) input call the same paths.

## Spaces

| Space          | Origin / axes                                            | Units       | Used for                                      |
|----------------|----------------------------------------------------------|-------------|-----------------------------------------------|
| **SCREEN**     | Bottom-left of the OS window (libGDX backbuffer)         | Pixels      | Raw pointer events, fullscreen UI             |
| **SURFACE**    | Bottom-left of the current render target (backbuffer/FBO) | Pixels      | GL viewport, active-pass `SceneViewport`      |
| **WORLD**      | ECS `Transform` space                                    | World units | Entities, simulation, picks                   |
| **NORMALIZED** | Bottom-left of the camera viewport rect on the surface   | 0..1        | Config anchors (`0.5, 0.5` = center)          |

## Rules

1. Hardware pointer → **SCREEN**. Convert to **SURFACE** with `ViewportService.mapScreenToSurface` when drawing to an FBO or letterboxed backbuffer.
2. Gameplay picks → **WORLD** via `SceneViewport.screenToWorld` or `engine.viewport().screenToWorld(...)`.
3. Never mix window dimensions into projection when drawing to an FBO — always use **SURFACE** size for that pass.
4. Orthographic camera `Transform.x/y` = center of the visible world rect.
5. Runtime sizing (`ViewportService.onWindowResize`, FBO pool, `glViewport`) always uses **physical backbuffer pixels** (`Gdx.graphics.getBackBufferWidth/Height` via `BackbufferSize`). Do not use `Gdx.graphics.getWidth/Height` for render sizing on desktop — on Retina they are logical points and cause quarter-frame rendering.

## Engine API

```java
HermesEngine engine = ...;
ViewportService vp = engine.viewport();

// Backbuffer pick (full-screen games)
Vec3 world = new Vec3();
vp.screenToWorld(world, pointerX, pointerY, 0f, world);

// Per-surface (FBO workflows)
RenderSurfaceDesc surface = vp.backbufferSurface(world);
Vec2 surfacePx = new Vec2();
vp.mapScreenToSurface(pointerX, pointerY, surface, surfacePx);
vp.forSurface(world, surface).screenToWorld(surfacePx.x, surfacePx.y, 0f, world);
```

## Render pipeline integration

Each outer `TargetBindingGraphPass`:

1. Binds the pass target (backbuffer or FBO).
2. Builds a `RenderSurface` from FBO/window pixel size + camera `fitMode` / `designAspect`.
3. Resolves the camera with `CameraResolver.resolveForPass(world, targetId, surfaceW, surfaceH)`.
4. Binds `SceneCamera` via `ViewportCameraBinder`, applies `glViewport` to the letterbox rect.
5. Passes `BoundCamera` to `World3dPass`, `SpritesPass`, `UiPass`, or `RenderContext` for custom passes.

`Camera.renderTarget` in scene JSON links a camera entity to a pipeline framebuffer id (see [render-pipeline.md](render-pipeline.md)).

## Related docs

- [render-pipeline.md](render-pipeline.md) — pass targets and FBO sizing
- [scene-format-v1.md](scene-format-v1.md) — `Camera` JSON (`fitMode`, `lookAt`, `renderTarget`)
- Unified input plan — `InputService` delegates picks to `ViewportService` (no separate mapper)
