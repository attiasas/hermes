# Hermes architecture

Hermes is a Java-first game engine on [libGDX](https://libgdx.com/). User game code compiles against a small public API; platform backends and libGDX stay inside engine modules and launchers.

## Module graph

```
game ──api──► hermes-api ◄── hermes-core (+ libGDX, internal)
  │              ▲
  └──runtime──► hermes-core
                      ▲
        hermes-launcher-{desktop,html,android}
```

| Module | Role |
|--------|------|
| `hermes-api` | Public types: `HermesApplication`, ECS (`World`, `Component`, `System`), scene stack (`SceneManager`, `SceneChangeRequest`), scene-facing components (`Transform`, `Sprite`, `Camera`). No libGDX. |
| `hermes-core` | Engine implementation: `SceneManagerImpl`, scene load, ECS runtime, rendering. Depends on `hermes-api` and libGDX (not exposed to game compile classpath). |
| `hermes-launcher-*` | Platform entrypoints (LWJGL3, TeaVM, Android). Depend on `hermes-core`. Included by the settings plugin when enabled. |
| `game` | Sample / dogfood game. `api` → `hermes-api`, `runtimeOnly` → `hermes-core`. |
| `hermes-tooling` | Shared non-Gradle logic: `hermes.json` parsing, doctor checks, `HERMES_HOME` resolution, engine version metadata. |
| `hermes-gradle-plugin` | Gradle settings + game plugins (`dev.hermes.settings`, `dev.hermes`). Composite `includeBuild` in the monorepo; published for templates. |
| `hermes-cli` | `hermes new`, `hermes doctor`, `hermes --version`. Depends on `hermes-tooling` artifact. |

Root `settings.gradle` includes: `hermes-api`, `hermes-core`, `hermes-tooling`, `game`, `hermes-cli`. Launcher modules are **not** listed there; `HermesSettingsPlugin` includes `hermes-launcher-*` from the repo root (monorepo) or from `.hermes/platforms/` (standalone games).

## libGDX boundary

- Game sources must not import `com.badlogicgames.gdx`. Enforced by `hermesDoctor` / `HermesDoctorSupport`.
- libGDX types appear only in `hermes-core` and launcher modules.
- Custom components implement `dev.hermes.api` types; systems run in the ECS without requiring libGDX for transform-only logic.

## Resolving the engine: monorepo vs Maven local vs `HERMES_HOME`

`HermesDependencyResolver` wires `game` dependencies in this order:

1. **Sibling projects** — If `:hermes-api` and `:hermes-core` exist in the same Gradle build (engine monorepo), use `api` / `runtimeOnly` project dependencies.
2. **Maven local** — Otherwise resolve `dev.hermes:hermes-api` and `dev.hermes:hermes-core` at `hermes.engineVersion` from `mavenLocal()` (+ `mavenCentral()`).

Standalone projects created with `hermes new` use (2). Publish once from the engine repo:

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

**`HERMES_HOME`** — Optional path to a Hermes engine checkout (`HERMES_HOME` env or `hermes.home` in `gradle.properties`). Used to:

- Sync launcher stubs from a live checkout into `.hermes/platforms/` when the bundled plugin JAR is not used.
- Satisfy doctor checks and composite-style workflows during engine development.

Doctor messaging treats Maven local + published version as the primary path for template users; `HERMES_HOME` is optional when artifacts are already in `~/.m2`.

## `.hermes/platforms/` lifecycle

Standalone game repos contain only the `game` module. Platform launchers live under `.hermes/platforms/`:

| Path | Contents |
|------|----------|
| `.hermes/platforms/hermes-launcher-desktop` | LWJGL3 desktop launcher Gradle project |
| `.hermes/platforms/hermes-launcher-html` | TeaVM HTML launcher |
| `.hermes/platforms/hermes-launcher-android` | Android launcher |
| `.hermes/version` | Engine version used when sync ran |

**When sync runs:** `HermesSettingsPlugin` calls `HermesPlatformSync.syncIfNeeded` before including a launcher. If the directory is missing or incomplete:

1. If `HERMES_HOME` points at a valid checkout (`hermes-api` + `hermes-core` dirs), copy launcher trees from there.
2. Else extract launcher trees bundled inside the published `hermes-gradle-plugin` JAR.

After copy, the plugin **renders** `build.gradle` from templates under `hermes-templates/platforms/<launcher>/build.gradle.tpl` (bundled in the Gradle plugin JAR). Tokens include `{{hermesCoreDependency}}` (Maven `dev.hermes:hermes-core` at `hermes.engineVersion`), `{{gameDependency}}` (`implementation` vs `compileOnly` for HTML TeaVM), and `{{agpVersion}}` for Android. Desktop uses JDK 17 toolchain with `release = 11` for Construo; HTML/Android use Java 11. Android templates omit project-level `repositories` blocks so root `dependencyResolutionManagement` applies.

**Refresh:** `./gradlew hermesSyncPlatforms` (or root `hermesSyncPlatforms`). Re-run after upgrading `hermes.engineVersion` or when doctor reports stale launchers.

In the **monorepo**, launcher sources live at repo root (`hermes-launcher-desktop/`, etc.); the settings plugin includes those directories directly when platforms are enabled—no `.hermes/platforms/` copy unless you are testing template-style layout.

## Tooling kernel and consumers

`hermes-tooling` is the single source of truth for:

- Parsing and validating `hermes.json`
- Standalone doctor checks (JDK, SDK, `HERMES_HOME`, Maven coordinates)
- `HermesHomeResolver`, `HermesEngineVersions`

| Consumer | Integration (target) |
|----------|----------------------|
| `hermes-cli` | `implementation project(':hermes-tooling')` / published `hermes-tooling` JAR |
| `hermes-gradle-plugin` | Depends on **`hermes-tooling` artifact**, not a duplicated source tree (today the plugin still adds `../hermes-tooling/src/main/java` to `sourceSets`; that compile-time copy is slated for removal) |

Plugin-specific code stays in `dev.hermes.gradle` (tasks, platform DSL, export, sync). CLI commands stay in `dev.hermes.cli`.

### Target package map (tooling / plugin / CLI)

Logical subpackages to grow into (names illustrative; flat packages today):

| Area | `dev.hermes.tooling` | `dev.hermes.gradle` | `dev.hermes.cli` |
|------|----------------------|---------------------|------------------|
| Config | `config` — `HermesGameConfig`, parser | `config` — DSL ↔ tooling models | — |
| Doctor | `doctor` — `HermesDoctorSupport`, SDK validator | `doctor` — `HermesDoctor`, Gradle task wiring | `doctor` — `DoctorCommand` |
| Home / versions | `home` — `HermesHomeResolver` | `home` — settings-level resolver | `home` — `HermesHomeDetector` |
| Platforms | — | `platform` — specs, sync, launcher include | `template` — `TemplateSupport`, `NewCommand` |
| Export | — | `export` — Construo/TeaVM/APK ZIP tasks | — |

## Platform template model

- **Project templates** live under `hermes-templates/minimal/` and `hermes-templates/multi-scene/`. `hermes new` copies a template and substitutes package/name placeholders via `TemplateEngine`.
- **Platform build templates** live under `hermes-templates/platforms/<launcher-module>/build.gradle.tpl`. `HermesPlatformSync` copies launcher **sources** (Java, resources, manifests) from the plugin JAR or `HERMES_HOME`, then renders `build.gradle` with `PlatformTemplateRenderer` and `PlatformSyncContext` (no regex patching).
- **Launchers** are not copied into the user repo as first-class modules; they are synced into `.hermes/platforms/` and included by path via `settings.project(...).setProjectDir(synced)`.
- **Engine JARs** come from Maven local at `hermes.engineVersion`; templates intentionally avoid `includeBuild('hermes-gradle-plugin')` so IDEs show only `game`.
- **Platform toggles** in `settings.gradle` (`hermes { platforms { desktop/html/android { enabled } } }`) control which launchers are included; run/export options live in `game/build.gradle` under `hermes { platforms { … } }`.

## Gradle plugin in the monorepo

```groovy
pluginManagement {
  includeBuild('hermes-gradle-plugin')
}
```

Dogfood builds use the composite plugin. CI and releases also run `:hermes-gradle-plugin:publishToMavenLocal` so integration tests and templates match the published plugin.

## Pre-release breaking-change policy

Hermes is **pre-release** (`0.1.0-SNAPSHOT`). Until 1.0:

- Scene format, Gradle DSL keys, task names, and template layout may change without a major-version bump.
- Prefer updating `docs/scene-format-v1.md` and migration notes in the same PR as breaking changes.
- After pulling engine changes, republish to Maven local and run `hermesSyncPlatforms` in game projects.
- Report incompatibilities via issues/PRs; avoid relying on undocumented behavior across engine upgrades.

## Scene manager

Games interact with scenes through `HermesEngine.scenes()` (`SceneManager`):

- **Registration** — `scenes().registry().register(id, assetPath)` or a full `SceneDefinition`.
- **Transitions** — queue `SceneChangeRequest.goTo`, `push`, or `pop`; the launcher calls `processPending()` each frame.
- **Active world** — `scenes().activeWorld()` for the top scene; `visibleScenes()` for bottom-to-top rendering.

Bootstrap: the launcher registers the `hermes.json` `scene` path as `"main"`, calls `onCreate` (component/system registration), then requests `goTo("main")`. See [Scene management](scene-management.md).

## Related docs

- [Scene management](scene-management.md)
- [Scene format v1](scene-format-v1.md)
- [Contributing](CONTRIBUTING.md)
- [Docs index](README.md)
