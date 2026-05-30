# Audio

Hermes routes **all** game audio through `AudioService` on `HermesEngine`. Authors can drive most games from JSON assets alone; Java code and SPI remain available for custom emitters and programmatic playback.

Desktop and Android use libGDX `Sound` / `Music` under the hood. The public API in `hermes-api` is libGDX-free.

## Quick start

1. Add `audio/profile.json` under your assets root (or set `"audioProfile"` in `hermes.json`).
2. Put clip files under `assets/sfx/`, `assets/music/`, etc.
3. Reference clip ids from ECS components, scene JSON, or input actions.

```json
{
  "title": "MyGame",
  "scene": "scenes/main.json",
  "audioProfile": "audio/profile.json"
}
```

When `audioProfile` is omitted, the runtime default is `audio/profile.json`. If that file is missing, the engine logs once and continues with a no-op profile.

## Complexity tiers

| Tier | You write | Engine does |
|------|-----------|-------------|
| **0 — Scene BGM** | `audio/bgm/main.json` + scene `"audio": { "bgm": "main" }` | Crossfade BGM on scene enter/exit via `SceneStack` |
| **1 — Entity templates** | `entities/campfire/type.json` with `AmbientSource` | `AmbientAudioSystem` plays 3D loop at entity position |
| **2 — Scene components** | Inline `FootstepEmitter` / `SoundEmitter` on entities | Built-in systems emit on movement, spawn, or interval |
| **3 — Action sounds** | `actionSounds` in profile + input/UI actions | `AudioActionSystem` plays on `input.actions().justPressed` |
| **4 — Code & SPI** | `engine.audio().play(...)`, custom `System`, `ComponentRegistration` | Full programmatic control |

**v1 limit:** Declarative `"playOn": "component"` triggers (e.g. play when `Health` changes) require a custom `System` or a future event bus.

## Config files

### `audio/profile.json`

```json
{
  "version": 1,
  "buses": {
    "master": 1.0,
    "sfx": 1.0,
    "music": 0.7,
    "ambient": 0.9
  },
  "clips": {
    "footstep": "sfx/footstep.wav",
    "hit": "sfx/hit.wav",
    "ui_click": "sfx/ui_click.wav"
  },
  "actionSounds": {
    "ui.click": "ui_click",
    "jump": "footstep"
  },
  "limits": {
    "maxInstancesPerClip": 8
  }
}
```

| Field | Description |
|-------|-------------|
| `clips` | Map of clip id → asset path (relative to assets root) |
| `actionSounds` | Input action name → clip id (or raw path) |
| `buses` | Default bus volumes (`master`, `sfx`, `music`, `ambient`) |
| `limits.maxInstancesPerClip` | Polyphony cap per clip (oldest instance stopped when exceeded) |

### `audio/bgm/*.json` — playlists

```json
{
  "version": 1,
  "mode": "random",
  "tracks": [
    "music/overworld_a.ogg",
    "music/overworld_b.ogg"
  ],
  "crossfadeSeconds": 2.0
}
```

| `mode` | Behavior |
|--------|----------|
| `sequential` | Play tracks in order, loop playlist |
| `random` | Pick next track at random |
| `single` | Play first track only |

Scene `"audio".bgm` resolves to `audio/bgm/{bgm}.json`. Use `"bgmPlaylist"` for an explicit playlist path.

### Scene `"audio"` block

```json
{
  "audio": {
    "bgm": "overworld",
    "fadeInSeconds": 1.5,
    "fadeOutSeconds": 1.0,
    "pauseBgmOnPause": false
  }
}
```

| Field | Default | Description |
|-------|---------|-------------|
| `bgm` | — | Playlist id → `audio/bgm/{bgm}.json` |
| `bgmPlaylist` | — | Explicit playlist path (overrides `bgm`) |
| `fadeInSeconds` | `1.0` | Crossfade in on scene enter |
| `fadeOutSeconds` | `1.0` | Fade out on scene exit (when this scene owns BGM) |
| `pauseBgmOnPause` | `false` | Pause BGM when scene stack pauses this scene (e.g. pause menu overlay) |

`SceneStack` calls `engine.audio().onSceneEnter/Exit/Pause/Resume` automatically — do not call these from game code unless implementing custom lifecycle.

## ECS audio components

All audio ECS systems use `WorldManager` and run at `ACTIVE_SCENE` scope (except `AudioActionSystem`, which is `GLOBAL`).

### `AmbientSource` — 3D positional loop

Requires `Transform` on the same entity.

| Property | Default | Description |
|----------|---------|-------------|
| `clip` | required | Asset path or profile clip id |
| `clipIsId` | `false` | Resolve `clip` via profile `clips` |
| `bus` | `"ambient"` | `sfx` or `ambient` |
| `volume` | `1` | Base volume 0..1 |
| `loop` | `true` | Loop playback |
| `minDistance` | `1` | Full volume inside this distance |
| `maxDistance` | `50` | Silent beyond this distance |
| `refDistance` | `1` | Reference distance (reserved for future backends) |

Entity template example — `entities/campfire/type.json`:

```json
{
  "version": 1,
  "components": {
    "Transform": { "x": 0, "y": 0, "z": 0 },
    "AmbientSource": {
      "clip": "ambient/fire_loop.wav",
      "minDistance": 2,
      "maxDistance": 25,
      "volume": 0.8
    }
  }
}
```

### `SoundEmitter` — one-shot / looped SFX

| Property | Default | Description |
|----------|---------|-------------|
| `clip` | required | Asset path or profile clip id |
| `clipIsId` | `false` | |
| `bus` | `"sfx"` | `sfx` or `ambient` |
| `volume` | `1` | |
| `pitch` | `1` | |
| `loop` | `false` | |
| `playOn` | `"manual"` | `spawn`, `interval`, or `manual` |
| `intervalSeconds` | `0` | For `interval` mode |

### `FootstepEmitter` — movement-driven steps

Requires `Transform`. Uses transform delta and `minSpeed` / `intervalSeconds`.

| Property | Default | Description |
|----------|---------|-------------|
| `clips` | required | Array of paths or clip ids |
| `clipIsId` | `false` | |
| `intervalSeconds` | `0.35` | Minimum time between steps |
| `minSpeed` | `0.5` | World units/sec to emit |
| `bus` | `"sfx"` | |
| `volume` | `0.6` | |

## Action-triggered sounds (no Java)

1. Define actions in `input/profile.json` (e.g. `"ui.click"`).
2. Map them in `audio/profile.json` `"actionSounds"`.
3. Bind UI buttons with `"action": "ui.click"` in UI JSON, or use keyboard/gamepad bindings.

`AudioActionSystem` runs each frame in `GLOBAL` scope and plays the mapped clip on `input.actions().justPressed(action)`.

## Programmatic playback

```java
SoundHandle hit = engine.audio().play(ClipId.of("hit"), PlayOptions.builder()
    .bus(AudioBus.SFX)
    .volume(0.9f)
    .pitch(0.95f + random.nextFloat() * 0.1f)
    .worldPosition(targetX, targetY, targetZ)
    .build());
```

### BGM from code

Prefer the scene `"audio"` block when possible. For dynamic logic:

```java
engine.audio().bgm().crossfadeTo("audio/bgm/boss.json", 2f);
```

### Session mixer (settings)

```java
ctx.session().mixer().setVolume(AudioBus.MASTER, userSettings.masterVolume);
```

`HermesSession.mixer()` is session-scoped and survives scene changes. Wire the same instance in `HermesApplication.createSession()`; future save/load may persist bus volumes via `SessionPersistable`.

## Frame order

After input poll and `GLOBAL` systems (including `AudioActionSystem`):

1. `engine.audio().tick(delta, activeManager, width, height)` — listener + BGM crossfade
2. `ACTIVE_SCENE` systems — `AmbientAudioSystem`, `SoundEmitterSystem`, `FootstepSystem`
3. Render pipeline

The 3D listener follows the active scene camera `Transform` via `CameraResolver`.

## Clip resolution

| Reference | Resolved to |
|-----------|-------------|
| `"sfx/hit.wav"` | Asset path as-is |
| Profile id `"hit"` | `audio/profile.json` → `clips.hit` |
| `"bgm": "overworld"` | `audio/bgm/overworld.json` |

## libGDX limits (v1)

| Feature | Status |
|---------|--------|
| SFX volume, pitch, pan | Supported via `PlayOptions` and `SoundHandle` |
| BGM crossfade / playlists | Supported via `BgmController` |
| BGM pitch | **Not supported** on streaming `Music` — use short looped `Sound` or custom code |
| 3D OpenAL positioning | Partial — positions are stored; libGDX 1.14 desktop `Sound` has limited 3D API |
| HTML / TeaVM | **Not in v1** — document for authors targeting web |

## Performance notes

- Clips load once via internal caches; never call `newSound` / `newMusic` per frame.
- Respect `limits.maxInstancesPerClip` in profiles.
- Audio systems stop handles when entities are removed.
- Prefer `.ogg` for music and short `.wav` for SFX on desktop.

## Extension points

| Extension | Approach |
|-----------|----------|
| Custom emitter component | `ComponentRegistration` SPI + custom `System` |
| Event-driven SFX | Subscribe in a `System` or future event bus |
| Save/load mixer prefs | Future `SessionPersistable` on session mixer |
| HTML audio | Implement `SoundBackend` / `MusicBackend` for TeaVM |

## Related docs

- [Scene format v1](scene-format-v1.md) — `"audio"` field and component tables
- [Input](input.md) — action names for `actionSounds`
- [Entity types](entity-types.md) — templates with `AmbientSource`
- [Scene management](scene-management.md) — stack lifecycle
- [Coordinate spaces](coordinate-spaces.md) — world units for 3D audio
