# World space and scene camera

Scene JSON can declare **world bounds**, **spatial search**, and a **main camera** without spawning camera entities.

## World block (version 1)

```json
{
  "world": {
    "version": 1,
    "kind": "open",
    "dimensions": { "width": 4000, "height": 3000 },
    "spatial": { "strategy": "uniformGrid", "cellSize": 128 }
  }
}
```

Omit `"world"` for an unbounded open world with brute-force spatial queries.

| Field | Description |
|-------|-------------|
| `world.version` | Must be `1`. |
| `world.kind` | `"open"` (default) or `"tilemap"` (tilemap loader — planned). |
| `world.dimensions` | Explicit `width`/`height`/`depth`, or `{ "match": "designViewport" }`. |
| `world.spatial.strategy` | `"bruteForce"` (default), `"uniformGrid"`, or `"tilemap"`. |
| `world.spatial.cellSize` | Grid cell size in world units (default `128`). |

## Camera block (version 1)

```json
{
  "camera": {
    "version": 1,
    "projection": "perspective",
    "x": 0,
    "y": 0,
    "z": 5,
    "fieldOfView": 60,
    "fitMode": "stretch",
    "lookAt": { "x": 0, "y": 0, "z": 0 },
    "controls": {
      "mode": "orbit",
      "rotateButton": "LEFT",
      "translateButton": "RIGHT",
      "forwardButton": "MIDDLE",
      "scrollZoom": true
    },
    "follow": "player"
  }
}
```

| `controls` field | Description |
|------------------|-------------|
| `mode` | `"orbit"` (default) or `"firstPerson"` |
| `enabled` | `false` to disable built-in controls (default off for orthographic cameras) |
| `rotateButton` / `translateButton` / `forwardButton` | `LEFT` / `RIGHT` / `MIDDLE` |
| `scrollZoom` | Scroll wheel dolly when `true` |
| `rotateAngle`, `translateUnits`, `scrollFactor` | Sensitivity (libGDX defaults) |
| `velocity`, `degreesPerPixel` | First-person mode speed / look sensitivity |

The scene `"camera"` block drives the main view. Entity `Camera` components are for auxiliary passes (`renderTarget`, e.g. minimap) or runtime `bindMain("entity-id")`.

## Java API

```java
WorldManager manager = engine.scenes().activeManager();
manager.space().queryNear(x, y, radius);
manager.camera().bindMain("drone-cam");
```

See [scene-format-v1.md](scene-format-v1.md) for JSON fields and [coordinate-spaces.md](coordinate-spaces.md) for units.
