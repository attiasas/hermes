# Hermes

Hermes is a Java-first game engine layered on [libGDX](https://libgdx.com/). User-facing code targets a small public API (`hermes-api`); libGDX stays inside `hermes-core` and launcher modules.

**Vision and requirements:** [plan/PROJECT.md](plan/PROJECT.md) · **Roadmap:** [plan/hermes_engine_plan_80cb86d9.detailed.plan.md](plan/hermes_engine_plan_80cb86d9.detailed.plan.md)

---

## Phase 2 — JSON scenes + ECS

Scenes under the game module assets directory drive entities at startup. The sample scene [`game/src/main/resources/assets/scenes/main.json`](game/src/main/resources/assets/scenes/main.json) places the libGDX logo via `Transform` + `Sprite` and a `main-camera` entity (`Transform` + `Camera`, orthographic or perspective). See [docs/scene-format-v1.md](docs/scene-format-v1.md).

### Custom components

Implement `dev.hermes.api.Component`, register the type, and add a `System` that reads or mutates it each frame (no libGDX imports required for transform-only logic).

**Explicit (`onCreate`):** `BounceMarker` + `BounceMarkerSystem` in [`SampleHermesGame`](game/src/main/java/dev/hermes/sample/SampleHermesGame.java).

**ServiceLoader:** `SpinMarker` + `SpinMarkerSystem` via [`META-INF/services/...`](game/src/main/resources/META-INF/services/dev.hermes.api.ecs.ComponentRegistration) and [`SpinMarkerRegistration`](game/src/main/java/dev/hermes/sample/SpinMarkerRegistration.java).

### Verify

```bash
./gradlew :hermes-core:test
./gradlew :game:hermesRunDesktop
```

---

## Phase 3 — templates, CLI, and doctor

### Prerequisites for new projects

Publish engine artifacts to Maven local once (from this repo):

```bash
./gradlew publishToMavenLocal
```

From the Hermes engine repo, publish artifacts and the Gradle plugin once:

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

`hermes new` creates only the **`game`** module; the desktop launcher is synced under `.hermes/platforms/`. Engine JARs and the Hermes Gradle plugin resolve from **Maven local** (`hermes.engineVersion`) — no `includeBuild`, so IntelliJ does not show `hermes-gradle-plugin` as a module.

### CLI

```bash
./gradlew :hermes-cli:installDist
export PATH="$PWD/hermes-cli/build/install/hermes/bin:$PATH"

hermes new my-game --name MyGame --package dev.hermes.mygame --platforms desktop,html
cd my-game
hermes doctor
./gradlew :game:hermesRunDesktop
```

| Command | Description |
|---------|-------------|
| `hermes new <dir>` | Copy the empty template (`--template empty`, `--platforms`, `--android-sdk`) |
| `hermes doctor [dir]` | Run `./gradlew :game:hermesDoctor` in a project, or standalone checks |
| `hermes --version` | CLI / engine version |

Release tags attach `hermes-cli-*-{linux,macos,windows}-*.zip` per OS (see [`.github/workflows/release.yml`](.github/workflows/release.yml)).

### Gradle doctor

```bash
./gradlew :game:hermesDoctor
```

Fails the build if `com.badlogicgames.gdx` appears under `game/src/`. Sync platform stubs: `./gradlew hermesSyncPlatforms`.

### Empty template

Source: [`hermes-templates/empty`](hermes-templates/empty) — minimal scene, `PulseMarker` SPI example, desktop-only platforms by default in `settings.gradle`.

---

## Phase 1 — run from `:game`

### Prerequisites

- **JDK 11+** (toolchain targets Java 11; CI uses 17).
- Network access the first time Gradle resolves dependencies.
- **Android** (optional): Android SDK — see [Android SDK](#android-sdk) below.

### Quick start (desktop)

```bash
./gradlew :game:hermesRunDesktop
```

You should see a 640×480 window with the sample libGDX logo. Game logic lives in `:game` (`SampleHermesGame`); the engine loads it via `-Dhermes.applicationClass` from the Gradle plugin.

Cold-cache build:

```bash
./gradlew clean build
```

### Configuration split

| Source | Purpose |
|--------|---------|
| [`game/hermes.json`](game/hermes.json) | **Game data only** — `title`, `scene` (simulation/content). |
| [`settings.gradle`](settings.gradle) `hermes { platforms { … } }` | Platform toggles and per-platform settings (desktop / html / android). |
| [`game/build.gradle`](game/build.gradle) `hermes { … }` | `version`, `applicationClass`, `assetsDirectory`, `debug`. |

Example `hermes.json`:

```json
{
  "title": "HermesSample",
  "scene": "scenes/main.json"
}
```

Example Gradle DSL on `:game`:

```groovy
plugins {
  id 'dev.hermes'
}

version = '0.1.0'

hermes {
  applicationClass = 'dev.hermes.sample.SampleHermesGame'
  assetsDirectory = 'src/main/resources/assets'  // default; override to relocate assets
  debug = true
}
```

Enable HTML or Android in [`settings.gradle`](settings.gradle):

```groovy
hermes {
  platforms {
    html { enabled = true }
    android { enabled = true }
  }
}
```

Then sync Gradle and run:

```bash
./gradlew :game:hermesRunHtml
./gradlew :game:hermesRunAndroid   # requires device/emulator + SDK
```

### Hermes Gradle tasks (`:game`)

| Task | Description |
|------|-------------|
| `hermesRunDesktop` | LWJGL3 desktop run |
| `hermesRunHtml` | TeaVM dev server (when `hermes-launcher-html` is included) |
| `hermesRunAndroid` | Install debug APK and launch (when Android launcher is included) |
| `validateHermesJson` | Parse `hermes.json`; unknown keys log a warning |
| `hermesDoctor` | Validate setup, toolchains, and forbidden libGDX imports |
| `hermesSyncPlatforms` | Copy launcher stubs into `.hermes/platforms/` for standalone projects |
| `generateAssetList` | Regenerate `build/generated/hermes-assets/assets.txt` from the assets directory |

### Module graph

- `game` → `hermes-api` (compile); `hermes-core` (runtime).
- `hermes-core` → `hermes-api` + libGDX (internal).
- `hermes-launcher-*` → `hermes-core` (+ platform backends).
- `hermes-gradle-plugin` — composite-included via `includeBuild`; not a published subproject.

Verify no direct libGDX in game sources:

```bash
./gradlew :game:hermesDoctor
```

### Template provenance

Launchers adapt the local **gdx-liftoff** template (`libGDX 1.14.0`, LWJGL 3.4.1, TeaVM 1.5.5). See Phase 0 notes for `StartupHelper` (Apache 2.0, damios).

### Android SDK

Hermes resolves the SDK in this order (first match wins):

1. `sdk.dir` in [`local.properties`](local.properties) (gitignored; copy from [`local.properties.example`](local.properties.example))
2. `hermes.android.sdk` in [`gradle.properties`](gradle.properties)
3. `ANDROID_SDK_ROOT` or `ANDROID_HOME`

If the path comes from (2) or (3) and `local.properties` has no `sdk.dir`, the settings plugin writes `local.properties` for AGP.

Examples:

```properties
# local.properties
sdk.dir=/opt/homebrew/share/android-commandlinetools
```

```properties
# gradle.properties
hermes.android.sdk=/opt/homebrew/share/android-commandlinetools
```

```bash
export ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools
./gradlew :game:hermesRunAndroid
```

Enable the platform in [`settings.gradle`](settings.gradle) (`platforms.android.enabled = true`), then install platform/build-tools if needed (`sdkmanager "platforms;android-35" "build-tools;35.0.0"`).

### Troubleshooting

- **macOS:** `hermesRunDesktop` adds `-XstartOnFirstThread`; `StartupHelper` may restart the JVM when needed.
- **Linux + NVIDIA:** `__GL_THREADED_OPTIMIZATIONS=0` is set for Gradle-spawned runs.
- **Android:** See [Android SDK](#android-sdk); ensure a device/emulator is connected for `hermesRunAndroid`.
- **HTML:** Enable `platforms.html` in settings, then `hermesRunHtml` serves at http://localhost:8080/ after TeaVM build.

### CI and releases

- **CI:** [`.github/workflows/ci.yml`](.github/workflows/ci.yml) — `./gradlew clean build` on push (desktop-only platforms by default).
- **Releases:** [`.github/workflows/release.yml`](.github/workflows/release.yml) on tag `v*`.

### References

- [libGDX wiki](https://libgdx.com/wiki/)
- [libGDX Javadoc](https://javadoc.io/doc/com.badlogicgames.gdx)
