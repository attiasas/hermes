# Scene management

Hermes scenes are JSON entity graphs loaded into isolated ECS worlds. A **scene manager** owns registration, a **stack**
of loaded scenes, and a queue of **change requests** processed once per frame.

## Stack vs switch

| Operation        | API                           | Effect                                                                                                                                                         |
|------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Switch**       | `SceneChangeRequest.goTo(id)` | Clears the stack, loads the target scene, and makes it active. Use for level changes, main menu → game, or any full replacement.                               |
| **Push overlay** | `SceneChangeRequest.push(id)` | Pauses the current top scene, loads the new scene on top, and makes it active. Underlying scenes stay loaded and are **visible** for rendering (bottom → top). |
| **Pop overlay**  | `SceneChangeRequest.pop()`    | Exits and disposes the top scene, then **resumes** the scene below.                                                                                            |

The dogfood sample ([`dogfood-simulation/`](../dogfood-simulation/)) registers a `pause` scene and uses [
`SceneNavigationSystem`](../dogfood-simulation/src/main/java/dev/hermes/sample/SceneNavigationSystem.java) to push/pop it on a timer.
The multi-scene CLI template demonstrates the same pattern.

**Rendering:** global systems run once per frame; each **visible** scene in the stack gets `System.render(manager)` called
in stack order. **Updates** for `SystemScope.ACTIVE_SCENE` systems run only against the active (top) scene’s `WorldManager`.

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

| Scope          | `update`                                                    | Typical use                                         |
|----------------|-------------------------------------------------------------|-----------------------------------------------------|
| `GLOBAL`       | Every frame, active scene’s `WorldManager` passed in        | Input routing, scene navigation, debug HUD          |
| `ACTIVE_SCENE` | Only when a scene is active; uses that scene’s manager      | Gameplay logic tied to the top scene (movement, AI) |

Global systems still receive `render(manager)` for each visible scene from the launcher loop.

## Built-in pointer demos

Stock `main.json` scenes ship with `Selectable` entities and `input/profile.json` (pointer → `select`). The engine
registers three GLOBAL input systems in `BuiltinComponents`:

| Camera projection | Pointer behavior |
|-------------------|------------------|
| **Perspective (3D)** | Click entity → select; drag empty space → orbit active camera (`CameraSceneControlSystem`). |
| **Orthographic (2D)** | Click sprite → select; drag → move selected entity (`EntityDragSystem`). |

| Project | Scene | Demo |
|---------|-------|------|
| `dogfood-simulation` | `scenes/main.json` | 3D cube with `Selectable`; orbit on empty drag |
| `hermes-templates/minimal` | `scenes/main.json` | Same 3D cube demo |
| `hermes-templates/multi-scene` | `scenes/main.json` | Same; overlay pause scene via `SceneNavigationSystem` |
| `hermes-templates/2d` | `scenes/main.json` | Two sprites; click and drag to move |

Run dogfood: `./gradlew :dogfood-simulation:hermesRunDesktop`. Details: [input.md](input.md).

## Entity types

Scene JSON may set [`type`](scene-format-v1.md) or [`kind`](scene-format-v1.md) to load reusable templates from
`assets/entities/<kind>/type.json`. Registered kinds merge template components with scene overrides; unregistered kinds
are stored as tags only. Query by kind with `manager.entities().entitiesWithKind(EntityKind.of("…"))`.

See [entity-types.md](entity-types.md) for merge rules, `$ref`, and `spawn()`.

**Future scene types:** scripted/procedural sources (implement `SceneSource` without JSON), additive loading, and entity
save snapshots keyed by scene id + entity name/kind. Update [scene-format-v1.md](scene-format-v1.md) when entity JSON
rules change.

## Related docs

- [Input system](input.md) — profiles, picking, built-in selection and drag
- [Scene format v1](scene-format-v1.md) — JSON entity/component schema (`Material`, `Mesh`, `RenderLayer`, `Selectable`)
- [Entity types](entity-types.md) — reusable `type.json` templates, merge, `$ref`, spawn
- [Architecture](ARCHITECTURE.md) — module boundaries and launcher bootstrap order
