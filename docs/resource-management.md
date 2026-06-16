# Resource management

Hermes routes **all** game assets (textures, models, sounds, fonts, JSON, binary blobs) through a single `ResourceService` on `HermesEngine`. Authors can drive most games from JSON assets alone; Java code and SPI remain available for streaming, custom loaders, and manual cache control.

Desktop and Android decode assets on worker threads where safe. HTML (TeaVM) uses **cooperative frame-sliced loading** on the main thread — same JSON config, no background threads. See [HTML / TeaVM](#html--teavm) below.

## Quick start

1. Optional: add `resources/catalog.json` and `resources/bundles/*.json` under your assets root.
2. Optional: add `resources/profile.json` (or set `"resourceProfile"` in `hermes.json`).
3. Reference asset paths directly in scene/entity JSON, or use `@aliases` from the catalog.
4. For level transitions with a loading bar, add a `"preload"` block to scene JSON with `"async": true`.

```json
{
  "title": "MyGame",
  "scene": "scenes/main.json",
  "resourceProfile": "resources/profile.json",
  "loadingScreen": "ui/loading.json"
}
```

When `resourceProfile` is omitted, the runtime default is `resources/profile.json`. If that file is missing, the engine uses built-in defaults (catalog path `resources/catalog.json`, bundles dir `resources/bundles`, sync preload on desktop).

## Complexity tiers

| Tier | You write | Engine does |
|------|-----------|-------------|
| **0 — Implicit** | Scene/entity JSON with plain paths (`"texture": "logo.png"`) | Sync load on first use through one shared cache (same UX as before, unified lifecycle) |
| **1 — Preload bundle** | Scene `"preload": { "bundles": ["game"], "async": true }` + `resources/bundles/game.json` | Async load bundles before scene enter; optional loading screen overlay |
| **2 — Catalog aliases** | `"texture": "@logo"` in components + `resources/catalog.json` | Resolve alias once; rename files in one place |
| **3 — Java control** | `engine.resources().loadBundleAsync(...)`, `retain` / `release`, poll `LoadTicket` | Manual streaming, custom loading UX, scene-scoped release groups |
| **4 — Custom loader SPI** | `ResourceLoaderRegistration` for new `ResourceKind` values | Plug new asset types without editing core |

**v1 limits:** No HTTP/CDN streaming. No priority preemption (FIFO batches). No automatic memory budget eviction — use bundles and `releaseSceneResources`. Shader compilation stays in the render pipeline (not moved into the resource manager). Scene `preload.paths` is parsed and validated but not yet applied by `SceneStack` — list one-off assets in a bundle instead.

## Configuration files

All paths are relative to the game assets root (`hermes.assetsDirectory`, default `src/main/resources/assets/`).

### `hermes.json`

| Field | Default | Purpose |
|-------|---------|---------|
| `resourceProfile` | `resources/profile.json` | Catalog path, bundles directory, default sync/async policy |
| `loadingScreen` | built-in minimal bar | Optional UI document path for async scene transitions (custom UI bindings planned; v1 uses built-in bar) |

### `resources/profile.json`

```json
{
  "version": 1,
  "catalog": "resources/catalog.json",
  "bundlesDirectory": "resources/bundles",
  "defaultAsync": false,
  "htmlDefaultAsync": true,
  "cooperativeAssetsPerFrame": 1,
  "showLoadingScreenWhenAsync": true
}
```

| Field | Default | Purpose |
|-------|---------|---------|
| `catalog` | `resources/catalog.json` | Path to alias catalog |
| `bundlesDirectory` | `resources/bundles` | Directory containing `*.json` bundle files |
| `defaultAsync` | `false` | Desktop default when scene preload does not set `async` |
| `htmlDefaultAsync` | `true` | HTML: prefer cooperative frame-sliced preload (no background threads) |
| `cooperativeAssetsPerFrame` | `1` | HTML only: max Phase A decodes per `ResourceService.tick()` |
| `showLoadingScreenWhenAsync` | `true` | Show overlay during async scene transitions |

### `resources/catalog.json`

Maps stable `@aliases` to asset paths. Entry names **must** start with `@`.

```json
{
  "version": 1,
  "entries": {
    "@logo": { "path": "textures/hermes-logo.png", "kind": "texture" },
    "@player-model": { "path": "models/hero.obj", "kind": "model" },
    "@click": { "path": "sfx/ui/click.wav", "kind": "sound" }
  }
}
```

| `kind` value | Loads as |
|--------------|----------|
| `texture` | PNG/JPG → GPU texture (shared by sprites and UI) |
| `model` | Wavefront OBJ → libGDX `Model` |
| `sound` | WAV/OGG → sound handle (skipped on HTML v1) |
| `font` | Bitmap font |
| `json` | Raw JSON string cache |
| `binary` | Opaque bytes (for SPI loaders) |

Use aliases in scene components, bundles, and Java via `engine.resources().resolve("@logo")`.

### `resources/bundles/*.json`

Named preload groups. File `resources/bundles/main-menu.json` has `"id": "main-menu"`.

```json
{
  "version": 1,
  "id": "main-menu",
  "resources": [
    { "ref": "@logo", "kind": "texture" },
    { "ref": "models/cube.obj", "kind": "model" },
    { "ref": "sfx/ui/hover.wav", "kind": "sound" }
  ]
}
```

Each `ref` is a path or `@alias`. Bundles are loaded via scene preload (`"bundles": ["main-menu"]`) or Java (`loadBundleSync` / `loadBundleAsync`).

## Scene preload

Scenes may declare a top-level `"preload"` object. Full field reference: [scene-format-v1.md — Preload block](scene-format-v1.md#preload-block).

When `"async": true` and `"bundles"` is non-empty, `SceneStack` loads bundles asynchronously **before** entering the scene, shows the loading overlay, then parses entities. With `"async": false`, the scene enters immediately and assets load on first use (tier 0) unless you call `loadBundleSync` from Java.

```json
{
  "preload": {
    "async": true,
    "showLoadingScreen": true,
    "bundles": ["main-menu", "shared-ui"],
    "paths": [
      { "ref": "textures/background.png", "kind": "texture" }
    ]
  },
  "entities": []
}
```

Scene-level `"async"` overrides profile `defaultAsync` for that transition only.

## Loading screen

During async scene transitions, `SceneStack` shows a fullscreen loading overlay. Progress is aggregate across all bundles in the preload list.

**Built-in (default):** minimal progress bar rendered by `LoadingScreenController`.

**Custom UI (optional):** set `"loadingScreen": "ui/loading.json"` in `hermes.json`. Bind widgets to engine-provided keys (custom document rendering is stubbed in v1; built-in bar is used until UI bindings are wired):

```json
{
  "version": 1,
  "designSize": { "width": 640, "height": 360 },
  "root": {
    "type": "column",
    "children": [
      { "type": "label", "id": "title", "text": "Loading…" },
      { "type": "progressBar", "id": "progress", "binding": "loading.progress" }
    ]
  }
}
```

| Binding | Range | Description |
|---------|-------|-------------|
| `loading.progress` | 0.0–1.0 | Aggregate async load progress |
| `loading.label` | string | Current bundle id or status text |

The launcher calls `engine.resources().tick()` each frame and skips normal rendering while the loading screen is visible.

## Java API (`ResourceService`)

Access via `engine.resources()` after `onCreate`.

```java
ResourceService resources = engine.resources();

// Resolve path or @alias
ResourceRef logo = resources.resolve("@logo");

// Sync load (blocks calling thread)
resources.loadSync(logo, ResourceKind.TEXTURE);
resources.loadBundleSync("main-menu");

// Async load — returns ticket to poll
LoadTicket ticket = resources.loadBundleAsync("level-2");
while (!ticket.done() && !ticket.failed()) {
    float p = ticket.progress(); // 0.0 .. 1.0
    engine.resources().tick();   // required on HTML; safe on desktop
}
if (ticket.failed()) {
    ticket.error().ifPresent(Throwable::printStackTrace);
}

// Reference counting (tier 3 streaming)
resources.retain(logo, ResourceKind.TEXTURE);
resources.release(logo, ResourceKind.TEXTURE);
resources.retainSceneBundle("world", "chunk-0-0");
resources.releaseSceneResources("world");
```

### `LoadTicket` polling rules

| Method | Desktop | HTML / main loop |
|--------|---------|------------------|
| `done()` / `failed()` / `progress()` | Poll anytime | Poll anytime — **never block the main loop** |
| `awaitCompletion()` | OK from test threads | **Forbidden** on the game loop — freezes the tab |

On HTML, always advance loads with `engine.resources().tick()` once per frame. Tests should pump frames until `ticket.done()` instead of calling `awaitCompletion()` on the main thread.

## SPI — `ResourceLoaderRegistration`

Register custom loaders for built-in or extended `ResourceKind` values:

```java
public final class ProceduralMeshRegistration implements ResourceLoaderRegistration {
    @Override
    public void register(ResourceLoaderRegistry registry) {
        registry.register(ResourceKind.BINARY, new MyBinaryLoader());
    }
}
```

Register the class in:

```
META-INF/services/dev.hermes.api.resource.ResourceLoaderRegistration
```

The engine discovers implementations via `ServiceLoader` during `HermesEngineImpl` construction, before `onCreate`. Loader implementations live in `hermes-core` (decode + GPU upload contract); the SPI interface is in `hermes-api`.

See `hermes-core/src/test/java/dev/hermes/core/resource/TestBinaryResourceLoaderRegistration.java` for a minimal test registration.

## FAQ

**Sync vs async — when to use which?**

- **Sync (tier 0):** small games, unit tests, assets loaded on first draw. No loading screen.
- **Async + bundles (tier 1):** level loads, menus with many textures/models. Spreads decode/GPU upload across frames; shows loading bar on HTML and desktop.

**When to use bundles vs catalog?**

- **Catalog:** stable `@aliases` reused across scenes and components — rename/move files in one place.
- **Bundles:** named groups loaded together for a menu, level, or locale pack. Reference bundle ids from scene `"preload"` or Java.

**Do I need a resource profile file?**

No. Without `resources/profile.json`, defaults apply. Add the file when you need custom async policy or non-default catalog/bundle paths.

## HTML / TeaVM

Hermes HTML builds embed the entire game assets tree at compile time. Authors use **identical JSON** (`resources/catalog.json`, bundles, scene `preload`) on desktop and web.

### Asset packaging

| Aspect | Desktop / Android | HTML (TeaVM) |
|--------|-------------------|--------------|
| Asset source | `:game` `processResources` + generated `assets.txt` manifest | `TeaVMBuilder.addAssets(hermes.assets.dir)` at compile time |
| Runtime config | Classpath `hermes-runtime.properties` | Also embedded via `addAssets(hermes.runtime.config.dir)` |
| Path API | `Gdx.files.internal` via `HermesAssetPaths` | Same API — files pre-bundled into WASM/JS |
| Directory listing | Works | Works if directories are embedded at compile time |
| `assets.txt` | Used for libGDX enumeration | **Never read** on HTML |

Every path in catalogs, bundles, and scenes must exist under the game assets directory at **TeaVM compile time**. There is no runtime download.

### Cooperative async — no background threads

Browsers run Hermes on a single main thread. TeaVM does not support desktop-style `ExecutorService` or background worker threads for asset decode.

| Phase | Desktop | HTML |
|-------|---------|------|
| Phase A (read file, decode PNG/OBJ) | Worker thread pool | **Cooperative:** at most `cooperativeAssetsPerFrame` decodes per `tick()` |
| Phase B (GPU: `new Texture`, `Model`) | `Gdx.app.postRunnable` | Same — schedules on the next frame of the **same browser thread** |

`HermesGdxApplication.render()` calls `engine.resources().tick()` before scene updates when async loads are active. Async preload still improves UX via the loading bar and avoids one giant blocking hitch — it does **not** add true parallelism on HTML.

Profile fields for HTML:

| Field | Purpose |
|-------|---------|
| `htmlDefaultAsync` | When `true`, scene `"async": true` uses cooperative frame-sliced loading (default `true`) |
| `cooperativeAssetsPerFrame` | Decode/upload budget per frame (start with `1`; try `2` if frame time allows) |

### Supported resource kinds on HTML

| `ResourceKind` | HTML | Notes |
|----------------|------|-------|
| `TEXTURE` | Yes | PNG/JPG via `FileHandle` |
| `MODEL` | Yes | OBJ via `ObjLoader` |
| `JSON` | Yes | Catalog, bundles, UI docs |
| `BINARY` | Yes | Opaque bytes for SPI loaders |
| `FONT` | Yes | Avoid desktop-only native font paths |
| `SOUND` | **Skipped** | No Web Audio backend in v1; entries are skipped at load with a debug log |

### `.glb` forbidden — split glTF required

Do not put `.glb` files in bundles when targeting HTML. `hermesDoctor` reports an **error** when the HTML platform is enabled and a bundle references a `.glb` path.

For animated 3D on web, use **split glTF** (`.gltf` + external `.bin` + PNG textures) — not monolithic `.glb` with embedded buffers. See the [animations and drawables plan](superpowers/plans/2026-05-30-animations-and-drawables.md).

### Sound skipped on HTML

Bundle entries with `"kind": "sound"` are **skipped at runtime** on HTML. `hermesDoctor` emits a **warning** when HTML is enabled and bundles include sound entries — remove them for web builds or accept silent preload until TeaVM audio lands. See [audio.md](audio.md).

### Doctor checks for bundles

When the HTML platform is enabled in Gradle, `./gradlew :game:hermesDoctor` scans `resources/bundles/*.json`:

| Finding | Severity |
|---------|----------|
| Path ending in `.glb` | **Error** — fix before HTML export |
| `"kind": "sound"` entry | **Warning** — skipped at runtime on web |

Resolve catalog `@aliases` when scanning paths.

### Export size guidance

The entire assets tree ships in the WASM/JS download. Keep **boot bundles small** — only what the first scene needs. Split large levels into separate bundle files and preload them per scene transition rather than one giant `"boot"` bundle.

Serve HTML exports over HTTP when WASM is enabled (`serve.sh` in the export ZIP). See [CONTRIBUTING.md](CONTRIBUTING.md).

## Related docs

- [Scene format v1 — Preload block](scene-format-v1.md#preload-block)
- [Scene management](scene-management.md) — stack transitions that trigger preload
- [Runtime config](runtime-config.md) — `hermes.json` and Gradle DSL
- [Audio](audio.md) — clip profiles (separate from resource bundles)
- [Animations plan](superpowers/plans/2026-05-30-animations-and-drawables.md) — future glTF kinds on top of this system
