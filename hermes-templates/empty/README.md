# {{PROJECT_NAME}}

Hermes game project generated from the empty template.

## Prerequisites

- **JDK 11+**
- Hermes engine and Gradle plugin published to Maven local (from the engine repo):

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

- **Android** (only if `android` is enabled in `settings.gradle`): Android SDK with `sdk.dir` in `local.properties` (see below)

This repo contains only the **`game`** module. Platform launchers are synced under `.hermes/platforms/` when you build or run — not the full Hermes engine tree.

## Quick start

```bash
./gradlew :game:hermesDoctor
```

### Desktop

```bash
./gradlew :game:hermesRunDesktop
```

Opens an LWJGL3 window. Window size and behavior are configured under `hermes { platforms { desktop { … } } }` in `settings.gradle`.

### HTML (TeaVM)

Requires `html { enabled = true }` in `settings.gradle`, then:

```bash
./gradlew :game:hermesRunHtml
```

Serves the build at `http://localhost:8080/` by default (`devServerPort` in `settings.gradle`).

### Android

Requires `android { enabled = true }` in `settings.gradle` and a valid SDK.

**SDK location:** use `local.properties` at the project root (gitignored):

```properties
sdk.dir=/path/to/Android/sdk
```

`hermes new --android-sdk /path/to/sdk` creates this file for you. You can also set `ANDROID_SDK_ROOT` / `ANDROID_HOME`; the Hermes settings plugin will write `local.properties` on first Gradle sync if it is missing.

Then connect a device or emulator and run:

```bash
./gradlew :game:hermesRunAndroid
```

## Configuration

| File | Purpose |
|------|---------|
| [`game/hermes.json`](game/hermes.json) | Game data: **`title`** (window / tab / app label), **`scene`** path |
| [`settings.gradle`](settings.gradle) | **`hermes { platforms { … } }`** — enable platforms and set platform-specific options |
| [`game/build.gradle`](game/build.gradle) | **`version`**, `hermes { applicationClass, debug, assetsDirectory }` |
| [`gradle.properties`](gradle.properties) | `hermes.engineVersion` (libGDX/LWJGL/TeaVM versions are injected by the settings plugin) |
| `local.properties` | **`sdk.dir`** for Android only (not committed) |

### `hermes.json` (game data)

```json
{
  "title": "{{PROJECT_NAME}}",
  "scene": "scenes/main.json"
}
```

`title` is used for the desktop window title, HTML page title, and Android app label.

### `settings.gradle` (platform toggles)

Only `enabled` per platform — controls which launcher modules are included.

### `game/build.gradle` (build / run / export)

| Platform | Settings |
|----------|----------|
| **desktop** | `width`, `height`, `vsync`, `resizable`, `foregroundFps`, `bundleId`, `executableName`, `exportTargets` |
| **html** | `width`, `height`, `devServerPort`, `webAssembly` |
| **android** | `applicationId`, `minSdk`, `targetSdk`, `compileSdk`, `versionCode`, `screenOrientation` |

Icons live under `src/main/resources/assets/icons/` (replace files to customize app icons).

Artifact **version** for stores and Android `versionName` comes from `version = '…'` in `game/build.gradle`, not from `hermes.json`.

### Useful Gradle tasks (`:game`)

| Task | Description |
|------|-------------|
| `hermesRunDesktop` | Run on desktop (LWJGL3) |
| `hermesRunHtml` | Build and serve HTML/TeaVM dev server |
| `hermesRunAndroid` | Install debug APK and launch on device |
| `hermesDoctor` | Validate setup, JDK, SDK, forbidden libGDX imports in game code |
| `validateHermesJson` | Parse and validate `hermes.json` |
| `hermesSyncPlatforms` | Refresh launcher stubs under `.hermes/platforms/` |
| `hermesExportDesktop` | Native desktop bundles (ZIP per OS) |
| `hermesExportHtml` | Static HTML/WASM site (ZIP) |
| `hermesExportAndroid` | Release APK (ZIP, unsigned) |
| `hermesExport` | All enabled exports |

Root task: `./gradlew hermesSyncPlatforms` (same sync, run from project root).

## Custom components

This template registers `PulseMarker` via `META-INF/services/dev.hermes.api.ecs.ComponentRegistration`.

Scene format: see [scene-format-v1.md](https://github.com/your-org/hermes/blob/main/docs/scene-format-v1.md) in the Hermes engine repo.

Example scene: [`game/src/main/resources/assets/scenes/main.json`](game/src/main/resources/assets/scenes/main.json).

## Troubleshooting

- **Plugins not found:** run `publishToMavenLocal` from the Hermes engine checkout; check `hermes.engineVersion` in `gradle.properties`.
- **Android SDK not found:** create `local.properties` with `sdk.dir=…` or pass `--android-sdk` when running `hermes new`.
- **HTML/Android task missing:** enable the platform in `settings.gradle`, sync Gradle, and run `hermesDoctor`.
