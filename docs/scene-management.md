# Scene management

Hermes scenes are JSON entity graphs loaded into isolated ECS worlds. A **scene manager** owns registration, a **stack**
of loaded scenes, and a queue of **change requests** processed once per frame.

## Stack vs switch

| Operation        | API                           | Effect                                                                                                                                                         |
|------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Switch**       | `SceneChangeRequest.goTo(id)` | Clears the stack, loads the target scene, and makes it active. Use for level changes, main menu → game, or any full replacement.                               |
| **Push overlay** | `SceneChangeRequest.push(id)` | Pauses the current top scene, loads the new scene on top, and makes it active. Underlying scenes stay loaded and are **visible** for rendering (bottom → top). |
| **Pop overlay**  | `SceneChangeRequest.pop()`    | Exits and disposes the top scene, then **resumes** the scene below.                                                                                            |

The sample game ([`game/`](../game/)) registers a `pause` scene and uses [
`SceneNavigationSystem`](../game/src/main/java/dev/hermes/sample/SceneNavigationSystem.java) to push/pop it on a timer.
The multi-scene CLI template demonstrates the same pattern.

**Rendering:** global systems run once per frame; each **visible** scene in the stack gets `System.render(world)` called
in stack order. **Updates** for `SystemScope.ACTIVE_SCENE` systems run only against the active (top) scene’s world.

**Lifecycle:** registered scenes may attach a `SceneLifecycle` (`onEnter`, `onExit`, `onPause`, `onResume`). Push/pop
and go-to invoke the matching hooks.

## SceneChangeRequest

Requests are **queued** via `engine.scenes().request(...)` and applied when the engine calls `processPending()` (once at
startup and each frame in the launcher).

```java
engine.scenes().request(SceneChangeRequest.goTo("level-2"));
engine.scenes().request(SceneChangeRequest.push("pause"));
engine.scenes().request(SceneChangeRequest.pop());
```

Do not call `processPending()` from game code unless you are writing tests; the launcher handles timing.

## Scene registration

Register every scene id before requesting it:

```java
engine.scenes().registry().register("main", "scenes/main.json");
engine.scenes().registry().register("pause", "scenes/pause.json");
```

Asset paths are relative to the game assets root (`hermes.assetsDirectory`, default `src/main/resources/assets/`).

For advanced setups, register a full `SceneDefinition` (custom `SceneSource`, `SceneLifecycle`, scene-local systems).
The convenience `register(String id, String assetPath)` loads JSON via the built-in scene loader.

The **bootstrap scene** path in `hermes.json` (`scene` field) is registered as `"main"` by the launcher, then `onCreate`
runs so custom components and extra scenes can be registered, then the launcher loads `"main"`. Register components in
`onCreate` (or via ServiceLoader before startup) before any scene JSON that references them is loaded.

## HermesSession

`HermesApplication.createSession()` returns a per-game session object shared across all scenes (player profile,
settings, progression). The engine binds it before `onCreate` and exposes it on `SceneContext.session()` during
lifecycle callbacks.

Default: `HermesSession.EMPTY` (no-op). Override when you need cross-scene state:

```java
@Override
public HermesSession createSession() {
  return new GameSession();
}
```

**Future:** save slots, audio mixer state, and other cross-scene services will live on `HermesSession` rather than
static singletons.

## SystemScope

When registering systems in `onCreate`:

```java
engine.addSystem(renderSystem);                              // GLOBAL (default)
engine.addSystem(new BounceMarkerSystem(), SystemScope.ACTIVE_SCENE);
```

| Scope          | `update`                                             | Typical use                                         |
|----------------|------------------------------------------------------|-----------------------------------------------------|
| `GLOBAL`       | Every frame, active world passed in                  | Input routing, scene navigation, debug HUD          |
| `ACTIVE_SCENE` | Only when a scene is active; uses that scene’s world | Gameplay logic tied to the top scene (movement, AI) |

Global systems still receive `render(world)` for each visible scene from the launcher loop.

## Entity kinds and saves (future)

Scene JSON may set an optional [`kind`](scene-format-v1.md) on entities (e.g. `"character"`, `"prop"`). Kinds are
metadata for queries (`World.entitiesWithKind`) and future persistence — they do not change loading behavior today.

**Future scene types:** scripted/procedural sources (implement `SceneSource` without JSON), additive loading, and entity
save snapshots keyed by scene id + entity name/kind. Update [scene-format-v1.md](scene-format-v1.md) when entity JSON
rules change.

## Related docs

- [Scene format v1](scene-format-v1.md) — JSON entity/component schema (`Material`, `Mesh`, `RenderLayer`)
- [Architecture](ARCHITECTURE.md) — module boundaries and launcher bootstrap order
