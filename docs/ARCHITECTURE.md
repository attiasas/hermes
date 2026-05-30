# Hermes architecture

Hermes is a Java-first game engine on [libGDX](https://libgdx.com/). User game code compiles against a small public API;
platform backends and libGDX stay inside engine modules and launchers.

## Module graph

```
dogfood-simulation ──api──► hermes-api ◄── hermes-core (+ libGDX, internal)
  │              ▲
  └──runtime──► hermes-core
                      ▲
        hermes-launcher-{desktop,html,android}
```

| Module                 | Role                                                                                                                                                                                               |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `hermes-api`           | Public types: `HermesApplication`, ECS (`WorldManager`, `EntityStore`, `Component`, `System`), entity types (`EntityTypeRegistry`), scene stack (`SceneManager`, `SceneChangeRequest`), `InputService`, `ViewportService`, scene-facing components (`Transform`, `Sprite`, `Camera`, `Selectable`). No libGDX. |
| `hermes-core`          | Engine implementation: `SceneManagerImpl`, scene load, ECS runtime, rendering. Depends on `hermes-api` and libGDX (not exposed to game compile classpath).                                         |
| `hermes-launcher-*`    | Platform entrypoints (LWJGL3, TeaVM, Android). Depend on `hermes-core`. Included by the settings plugin when enabled.                                                                              |
| `dogfood-simulation`   | Engine monorepo dogfood game. `api` → `hermes-api`, `runtimeOnly` → `hermes-core`.                                                                    |
| `hermes-tooling`       | Shared non-Gradle logic: `hermes.json` parsing, doctor checks, `HERMES_HOME` resolution, engine version metadata.                                                                                  |
| `hermes-gradle-plugin` | Gradle settings + game plugins (`dev.hermes.settings`, `dev.hermes`). Composite `includeBuild` in the monorepo; published for templates.                                                           |
| `hermes-cli`           | `hermes new`, `hermes doctor`, `hermes --version`. Depends on `hermes-tooling` artifact.                                                                                                           |

Root `settings.gradle` includes: `hermes-api`, `hermes-core`, `dogfood-simulation`, `hermes-cli`, and `hermes-launcher-*`.
`HermesSettingsPlugin` may also include launchers from `HERMES_HOME` (live checkout) or `.hermes/platforms/` (standalone games).

## libGDX boundary

- Game sources must not import `com.badlogicgames.gdx`. Enforced by `hermesDoctor` / `HermesDoctorSupport`.
- libGDX types appear only in `hermes-core` and launcher modules.
- Custom components implement `dev.hermes.api` types; systems run in the ECS without requiring libGDX for transform-only
  logic.

## Resolving the engine: monorepo vs Maven local vs `HERMES_HOME`

`HermesDependencyResolver` wires `game` dependencies in this order:

1. **Sibling projects** — If `:hermes-api` and `:hermes-core` exist in the same Gradle build (engine monorepo), use
   `api` / `runtimeOnly` project dependencies.
2. **Maven local** — Otherwise resolve `dev.hermes:hermes-api` and `dev.hermes:hermes-core` at `hermes.engineVersion`
   from `mavenLocal()` (+ `mavenCentral()`).

Standalone projects created with `hermes new` use (2). Publish once from the engine repo:

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

**`HERMES_HOME`** — Optional path to a Hermes engine checkout (`HERMES_HOME` env or `hermes.home` in
`gradle.properties`). When set, `HermesSettingsPlugin` can include launcher modules **in place** from that checkout
(`setProjectDir` under `HERMES_HOME/hermes-launcher-*`) without copying into `.hermes/platforms/`. `HermesPlatformSync`
also prefers `HERMES_HOME` as the source when populating the `.hermes/` cache. Doctor treats Maven local as the primary
path for template users; `HERMES_HOME` is optional when engine artifacts are already in `~/.m2`.

## Runtime configuration

Build-time settings (logging, window, scene paths, custom keys) are merged by `LaunchConfigResolver` in `hermes-tooling`
and written once to `hermes-runtime.properties`. Desktop, HTML, and Android launchers bundle or classpath that file;
runtime code reads it through `HermesRuntimeConfig` and `RuntimeConfigService`. See [runtime-config.md](runtime-config.md).

## User repos vs monorepo

**Standalone game repos (git)** track only the game module directory (default `game/`, or the name passed to
`hermes new --module`). `settings.gradle` sets `hermes { gameModule = '…' }` and `include`s that module alone. Launcher
projects are **not** committed: `.hermes/` is gitignored (see template `.gitignore`) and holds a local sync cache.

**Monorepo (engine checkout)** keeps launcher sources at the repo root (`hermes-launcher-desktop/`, etc.) and a dogfood
game module (`dogfood-simulation/`). `HermesSettingsPlugin` includes root launchers when present—no `.hermes/platforms/`
copy. The same `hermes-launcher-*` trees and thin `build.gradle` files are what get bundled into the Gradle plugin for
standalone sync.

## Launcher resolution order

`HermesSettingsPlugin` picks a launcher directory in this order:

1. **Repo root** — `hermes-launcher-*` next to `settings.gradle` (monorepo dogfood).
2. **`HERMES_HOME`** — `HERMES_HOME/hermes-launcher-*` included in place (live engine checkout; no `.hermes` copy).
3. **`.hermes/platforms/`** — synced cache for published-plugin users (default for `hermes new` projects).

| Path                                        | Role                                      |
|---------------------------------------------|-------------------------------------------|
| `.hermes/platforms/hermes-launcher-desktop` | Synced LWJGL3 desktop launcher project    |
| `.hermes/platforms/hermes-launcher-html`    | Synced TeaVM HTML launcher                |
| `.hermes/platforms/hermes-launcher-android` | Synced Android launcher                   |
| `.hermes/version`                           | `hermes.engineVersion` stamp after sync   |

## `.hermes/platforms/` sync

When path (3) is needed, `HermesPlatformSync.syncIfNeeded` copies **full** launcher modules (sources, manifests, thin
`build.gradle`, `.gitignore`) into `.hermes/platforms/`:

1. From `HERMES_HOME` when it is a valid checkout and contains the launcher directory.
2. Else from `hermes-platforms/<launcher>/` resources bundled in the `hermes-gradle-plugin` JAR.

There is no `hermes-templates/platforms/` and no `build.gradle` token rendering. Launcher Gradle config is applied by
`dev.hermes.launcher.desktop`, `dev.hermes.launcher.html`, and `dev.hermes.launcher.android`, which load shared Groovy
scripts (`launcher/desktop.gradle`, etc.) from the plugin. `HermesDependencyResolver` wires `hermes-api` / `hermes-core`
and the configured game module (Maven vs composite, HTML `compileOnly` game dep, etc.).

**Refresh:** `./gradlew hermesSyncPlatforms` re-extracts enabled launchers and updates `.hermes/version`. Run after
upgrading `hermes.engineVersion` or when doctor reports stale launchers.

## `hermes.gameModule`

`hermes.gameModule` in `settings.gradle` is **required** (no silent default). It names the game subproject that launchers
depend on and that hosts `hermesDoctor` / run / export tasks. `hermes new --module <name>` sets the directory name,
`include`, and `gameModule` (default `game`). `hermes doctor` reads `gameModule` from `settings.gradle` and delegates to
`:<gameModule>:hermesDoctor` when `gradlew` is present.

## Tooling kernel and consumers

`hermes-tooling` is the single source of truth for:

- Parsing and validating `hermes.json`
- Standalone doctor checks (JDK, SDK, `HERMES_HOME`, Maven coordinates)
- `HermesHomeResolver`, `HermesEngineVersions`

| Consumer               | Integration (target)                                                                                                                                                                                 |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `hermes-cli`           | `implementation project(':hermes-tooling')` / published `hermes-tooling` JAR                                                                                                                         |
| `hermes-gradle-plugin` | Depends on **`hermes-tooling` artifact**, not a duplicated source tree (today the plugin still adds `../hermes-tooling/src/main/java` to `sourceSets`; that compile-time copy is slated for removal) |

Plugin-specific code stays in `dev.hermes.gradle` (tasks, platform DSL, export, sync). CLI commands stay in
`dev.hermes.cli`.

### Target package map (tooling / plugin / CLI)

Logical subpackages to grow into (names illustrative; flat packages today):

| Area            | `dev.hermes.tooling`                            | `dev.hermes.gradle`                           | `dev.hermes.cli`                             |
|-----------------|-------------------------------------------------|-----------------------------------------------|----------------------------------------------|
| Config          | `config` — `HermesGameConfig`, parser           | `config` — DSL ↔ tooling models               | —                                            |
| Doctor          | `doctor` — `HermesDoctorSupport`, SDK validator | `doctor` — `HermesDoctor`, Gradle task wiring | `doctor` — `DoctorCommand`                   |
| Home / versions | `home` — `HermesHomeResolver`                   | `home` — settings-level resolver              | `home` — `HermesHomeDetector`                |
| Platforms       | `project` — `GameModuleNames`                   | `platform` — sync, launcher include; `launcher` — convention plugins | `template` — `TemplateSupport`, `NewCommand` |
| Export          | —                                               | `export` — Construo/TeaVM/APK ZIP tasks       | —                                            |

## Project templates

- **Project templates** live under `hermes-templates/minimal/` (3D), `hermes-templates/2d/` (orthographic sprites), and
  `hermes-templates/multi-scene/` (3D main + 2D overlay). `hermes new` copies a template and substitutes package/name
  and `gameModule` placeholders via `TemplateEngine`.
- **Launchers** are not first-class modules in the user repo; they resolve via `.hermes/platforms/`, `HERMES_HOME`, or
  (in the monorepo) repo-root `hermes-launcher-*` and are included with `settings.project(...).setProjectDir(...)`.
- **Engine JARs** come from Maven local at `hermes.engineVersion`; templates intentionally avoid
  `includeBuild('hermes-gradle-plugin')` so IDEs show only the game module.
- **Platform toggles** in `settings.gradle` (`hermes { platforms { desktop/html/android { enabled } } }`) control which
  launchers are included; run/export options live in the game module’s `build.gradle` under `hermes { platforms { … } }`.

## Gradle plugin in the monorepo

```groovy
pluginManagement {
  includeBuild('hermes-gradle-plugin')
}
```

Dogfood builds use the composite plugin. CI and releases also run `:hermes-gradle-plugin:publishToMavenLocal` so
integration tests and templates match the published plugin.

## Pre-release policy

Hermes is **pre-release** (`0.1.0-SNAPSHOT`). The scene format remains **v1** until 1.0; Gradle DSL keys, task names,
and templates may change without a v2 doc. Update [`docs/scene-format-v1.md`](scene-format-v1.md) in the same PR when
scene JSON rules change. After pulling engine changes, republish to Maven local and run `hermesSyncPlatforms` in game
projects.

## Scene manager

Games interact with scenes through `HermesEngine.scenes()` (`SceneManager`):

- **Registration** — `scenes().registry().register(id, assetPath)` or a full `SceneDefinition`.
- **Transitions** — queue `SceneChangeRequest.goTo`, `push`, or `pop`; the launcher calls `processPending()` each frame.
- **Active scene** — `scenes().activeManager()` for the top scene’s `WorldManager`; `visibleScenes()` for bottom-to-top rendering.

Bootstrap: the launcher registers the `hermes.json` `scene` path as `"main"`, scans `assets/entities/*/type.json` via
`engine.entityTypes().scanAssets()`, calls `onCreate` (component/system registration), then requests `goTo("main")`.
See [Scene management](scene-management.md).

## ECS

Each loaded scene owns one **`WorldManager`** — the simulation root for that scene. Systems receive `WorldManager` in
`update` / `render`; render and input APIs take **`EntityStore`** from `manager.entities()`.

| Type | Role |
|------|------|
| `WorldManager` | Per-scene root; today exposes `entities()` only |
| `EntityStore` | Entity storage, components, queries, `spawn(kind)` |
| `EntityTypeRegistry` | Template catalog (`entities/<kind>/type.json`); `engine.entityTypes()` |

Entity creation (scene load and `spawn`) runs through **`EntityFactory`**: merge type template + instance overrides →
`ComponentRefResolver` (`$ref`) → deserialize. Inline scene entities without a registered type skip template merge.

See [Entity types](entity-types.md) and [Scene format v1](scene-format-v1.md).

## Input

`HermesEngine.input()` exposes `InputService` (poll, remapped actions, device snapshots, screen picking). Profiles load
from `hermes.json` → `inputProfile` (asset path, default `input/profile.json`). Scene JSON may set `inputContext` to
switch binding sets while that scene is active.

Built-in **GLOBAL** systems in `BuiltinComponents` (no game Java for stock demos):

- `SelectionSystem` — pointer `select` action → `Selected` on `Selectable` hits
- `CameraSceneControlSystem` — perspective scenes: empty left-drag orbits active camera
- `EntityDragSystem` — orthographic scenes: drag moves `Selected` entity

Coordinate conversion for picks and drags uses `ViewportService` only (same path as rendering). See [input.md](input.md)
and [coordinate-spaces.md](coordinate-spaces.md).

## Related docs

- [Scene management](scene-management.md)
- [Input system](input.md)
- [Coordinate spaces & viewport service](coordinate-spaces.md)
- [Render pipeline](render-pipeline.md)
- [Scene format v1](scene-format-v1.md)
- [Entity types](entity-types.md)
- [Contributing](CONTRIBUTING.md)
- [Docs index](README.md)
