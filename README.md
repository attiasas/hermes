# Hermes

Hermes is a Java-first game engine layered on [libGDX](https://libgdx.com/). Game code targets a small public API (
`hermes-api`); libGDX stays inside `hermes-core` and launcher modules.

**Docs:
** [Architecture](docs/ARCHITECTURE.md) · [Contributing](docs/CONTRIBUTING.md) · [Scene format](docs/scene-format-v1.md)

---

## Quick start (engine repo)

### Prerequisites

- **JDK 17** for local development (CI uses 17). Subprojects compile **Java 11** bytecode.
- Network access the first time Gradle resolves dependencies.
- **Android** (optional): SDK — see [Android SDK](#android-sdk).

### Run the sample game

```bash
./gradlew :game:hermesRunDesktop
```

You should see a 640×480 window with a textured 3D cube (config-only scene JSON). Game logic lives in [
`SampleHermesGame`](game/src/main/java/dev/hermes/sample/SampleHermesGame.java); the Gradle plugin passes
`-Dhermes.applicationClass` at launch. Every 12 seconds the sample auto-switches to the `advanced-render` demo (animated
water plane + cube); you can also `goTo("advanced-render")` manually.

Full build:

```bash
./gradlew clean build
```

---

## Repository layout

| Module                         | Role                                                                    |
|--------------------------------|-------------------------------------------------------------------------|
| `game`                         | In-repo sample game (dogfood project)                                   |
| `hermes-api`                   | Public engine API (compile-only for games)                              |
| `hermes-core`                  | ECS, scenes, libGDX integration                                         |
| `hermes-tooling`               | Shared config, doctor, SDK helpers (published + used by plugin/CLI)     |
| `hermes-gradle-plugin`         | `dev.hermes` / `dev.hermes.settings` plugins (composite `includeBuild`) |
| `hermes-launcher-*`            | Desktop (LWJGL3), HTML (TeaVM), Android launchers                       |
| `hermes-cli`                   | `hermes new`, `hermes doctor`, `hermes --version`                       |
| `hermes-templates/minimal`     | Default scaffold for `hermes new`                                       |
| `hermes-templates/multi-scene` | Sample with pause overlay and scene stack                               |

---

## Scenes and ECS

Scenes under the game assets directory drive entities at startup. The sample [
`main.json`](game/src/main/resources/assets/scenes/main.json) is a config-only 3D scene (`Mesh` + `Material` +
perspective `Camera`). [`advanced-render.json`](game/src/main/resources/assets/demos/advanced-render.json) demonstrates
a custom render pipeline and `WaterPass` (opt-in demo, not scanned by `hermesDoctor` HTML shader check). A [
`pause.json`](game/src/main/resources/assets/scenes/pause.json) overlay is pushed on a timer.
See [docs/scene-format-v1.md](docs/scene-format-v1.md) and [docs/scene-management.md](docs/scene-management.md).

### Custom components

Implement `dev.hermes.api.Component`, register the type, and add a `System` that reads or mutates it each frame (no
libGDX imports in game code).

- **Explicit (`onCreate`):** [`BounceMarker`](game/src/main/java/dev/hermes/sample/BounceMarker.java) +
  `BounceMarkerSystem` in [`SampleHermesGame`](game/src/main/java/dev/hermes/sample/SampleHermesGame.java).
- **ServiceLoader:** [`SpinMarker`](game/src/main/java/dev/hermes/sample/SpinMarker.java) via [
  `META-INF/services/...`](game/src/main/resources/META-INF/services/dev.hermes.api.ecs.ComponentRegistration).

```bash
./gradlew :hermes-core:test
```

---

## Configuration

| Source                                                            | Purpose                                                            |
|-------------------------------------------------------------------|--------------------------------------------------------------------|
| [`game/hermes.json`](game/hermes.json)                            | Game data — `title`, `scene` path                                  |
| [`settings.gradle`](settings.gradle) `hermes { platforms { … } }` | Which launchers are included (`desktop` / `html` / `android`)      |
| [`game/build.gradle`](game/build.gradle) `hermes { … }`           | `applicationClass`, assets, `debug`, icons, window/export settings |

Example `hermes.json`:

```json
{
  "title": "HermesSample",
  "scene": "scenes/main.json"
}
```

Example `game/build.gradle`:

```groovy
plugins {
  id 'dev.hermes'
}

hermes {
  applicationClass = 'dev.hermes.sample.SampleHermesGame'
  assetsDirectory = 'src/main/resources/assets'
  debug = true
  platforms {
    desktop { width = 640; height = 480; executableName = 'HermesGame' }
    html { width = 640; height = 480 }
    android { applicationId = 'dev.hermes.game' }
  }
}
```

Enable extra platforms in [`settings.gradle`](settings.gradle):

```groovy
hermes {
  platforms {
    html { enabled = true }
    android { enabled = true }
  }
}
```

---

## Gradle tasks (`:game`)

| Task                  | Description                                                         |
|-----------------------|---------------------------------------------------------------------|
| `hermesRunDesktop`    | LWJGL3 desktop run                                                  |
| `hermesRunHtml`       | TeaVM dev server (when HTML launcher included)                      |
| `hermesRunAndroid`    | Install debug APK and launch (when Android launcher included)       |
| `validateHermesJson`  | Parse `hermes.json`; unknown keys log a warning                     |
| `hermesDoctor`        | Validate setup, toolchains, forbidden libGDX imports                |
| `hermesSyncPlatforms` | Sync launcher stubs into `.hermes/platforms/` (standalone projects) |
| `generateAssetList`   | Regenerate asset manifest from the assets directory                 |
| `hermesExportDesktop` | Native desktop zips (Construo)                                      |
| `hermesExportHtml`    | HTML/TeaVM zip                                                      |
| `hermesExportAndroid` | Unsigned APK zip                                                    |
| `hermesExport`        | All enabled platform exports                                        |

```bash
./gradlew :game:hermesRunHtml
./gradlew :game:hermesRunAndroid   # device/emulator + SDK required
./gradlew :game:hermesDoctor
```

---

## New game projects

Publish engine artifacts and the Gradle plugin once from this repo:

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

`hermes new` creates only the **`game`** module; launchers sync under `.hermes/platforms/`. Engine JARs and the plugin
resolve from **Maven local** (`hermes.engineVersion`) — no `includeBuild`, so IDEs do not show `hermes-gradle-plugin` as
a module.

### CLI

```bash
./gradlew :hermes-cli:installDist
export PATH="$PWD/hermes-cli/build/install/hermes/bin:$PATH"

hermes new my-game --name MyGame --package dev.hermes.mygame --platforms desktop,html
cd my-game
hermes doctor
./gradlew :game:hermesRunDesktop
```

| Command               | Description                                                                                     |
|-----------------------|-------------------------------------------------------------------------------------------------|
| `hermes new <dir>`    | Copy a project template (`--template minimal` or `multi-scene`, `--platforms`, `--android-sdk`) |
| `hermes doctor [dir]` | Run `./gradlew :game:hermesDoctor` or standalone checks                                         |
| `hermes --version`    | CLI / engine version                                                                            |

Template sources: [`hermes-templates/minimal`](hermes-templates/minimal), [
`hermes-templates/multi-scene`](hermes-templates/multi-scene).

---

## Export

Distribution builds use `hermes.debug=false` even when `debug = true` in `game/build.gradle`. Replace icons under
`src/main/resources/assets/icons/` (shipped in the minimal template).

| Task                  | Output                                                                          |
|-----------------------|---------------------------------------------------------------------------------|
| `hermesExportDesktop` | `game/build/dist/desktop/*-{linux-x64,macos-aarch64,macos-x64,windows-x64}.zip` |
| `hermesExportHtml`    | `game/build/dist/html/*-html.zip`                                               |
| `hermesExportAndroid` | `game/build/dist/android/*-android.zip`                                         |
| `hermesExport`        | All enabled platforms                                                           |

```bash
./gradlew :game:hermesExportDesktop
./gradlew :game:hermesExportHtml
./gradlew :game:hermesExportAndroid
```

---

## Module dependencies

- `game` → `hermes-api` (compile); `hermes-core` (runtime)
- `hermes-core` → `hermes-api` + libGDX (internal)
- `hermes-launcher-*` → `hermes-core` + platform backends
- `hermes-gradle-plugin` → `hermes-tooling`; composite-included via `includeBuild` for monorepo work; publishable with
  `:hermes-gradle-plugin:publishToMavenLocal`
- `hermes-cli` → `hermes-tooling` (Maven local)

Doctor fails the build if `com.badlogicgames.gdx` appears under `game/src/`.

---

## Android SDK

Resolution order (first match wins):

1. `sdk.dir` in [`local.properties`](local.properties) (copy from [
   `local.properties.example`](local.properties.example))
2. `hermes.android.sdk` in [`gradle.properties`](gradle.properties)
3. `ANDROID_SDK_ROOT` or `ANDROID_HOME`

If the path comes from (2) or (3) and `local.properties` has no `sdk.dir`, the settings plugin writes `local.properties`
for AGP.

```properties
# local.properties
sdk.dir=/path/to/Android/sdk
```

Enable Android in [`settings.gradle`](settings.gradle), then install platform/build-tools if needed (
`sdkmanager "platforms;android-35" "build-tools;35.0.0"`).

---

## Troubleshooting

- **`hermesRunDesktop` missing or fails with launcher error:** Desktop is enabled in `settings.gradle` but
  `hermes-launcher-desktop` was not synced. Run `./gradlew hermesSyncPlatforms` and ensure `dev.hermes.settings`
  resolves the engine version from Maven local.
- **macOS desktop frozen / exit 133 / instant exit:** `hermesRunDesktop` uses JDK 17, `-XstartOnFirstThread`, and
  `hermes.desktop.gradleRun=true` so `StartupHelper` does not spawn a child JVM. Dock icon is for exported bundles only.
- **Stale `.hermes/platforms` after upgrading Hermes** (e.g. compile errors for removed launcher files): run
  `./gradlew hermesSyncPlatforms`, or delete `.hermes/platforms/` and re-sync. Platform sources refresh automatically
  when `.hermes/version` does not match the engine version.
- **Linux + NVIDIA:** `__GL_THREADED_OPTIMIZATIONS=0` is set for Gradle-spawned runs.
- **Android:** See [Android SDK](#android-sdk); connect a device/emulator for `hermesRunAndroid`.
- **HTML:** Enable `platforms.html` in settings; `hermesRunHtml` serves at http://localhost:8080/ after TeaVM build.

Desktop launcher includes vendored `StartupHelper` from gdx-liftoff (Apache 2.0, damios).

---

## CI and releases

- **CI:** [`.github/workflows/ci.yml`](.github/workflows/ci.yml) — build, tests, export smoke on `main`
- **Releases:** [`.github/workflows/release.yml`](.github/workflows/release.yml) on tag `v*`; CLI zips per OS

---

## References

- [libGDX wiki](https://libgdx.com/wiki/)
- [libGDX Javadoc](https://javadoc.io/doc/com.badlogicgames.gdx)
