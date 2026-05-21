# {{PROJECT_NAME}}

Hermes game project generated from the empty template.

## Prerequisites

- **JDK 11+**
- Hermes engine and Gradle plugin published to Maven local (from the engine repo):

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

- **Android** (only if `android` is enabled in `settings.gradle`): Android SDK with `sdk.dir` in `local.properties` (see [Configuration](#configuration))

This repo contains only the **`game`** module. Platform launchers are synced under `.hermes/platforms/` when you build or run — not the full Hermes engine tree.

## Quick start

```bash
./gradlew :game:hermesDoctor
```

## Running

### Desktop

Requires `desktop { enabled = true }` in `settings.gradle`.

```bash
./gradlew :game:hermesRunDesktop
```

Opens an LWJGL3 window. Window size and behavior are configured under `hermes { platforms { desktop { … } } }` in `game/build.gradle`.

### HTML (TeaVM)

Requires `html { enabled = true }` in `settings.gradle`.

```bash
./gradlew :game:hermesRunHtml
```

Serves the build at `http://localhost:8080/` by default (`devServerPort` in `game/build.gradle`).

### Android

Requires `android { enabled = true }` in `settings.gradle` and a valid SDK.

**SDK location:** use `local.properties` at the project root (gitignored):

```properties
sdk.dir=/path/to/Android/sdk
```

`hermes new --android-sdk /path/to/sdk` creates this file for you. You can also set `ANDROID_SDK_ROOT` / `ANDROID_HOME`; the Hermes settings plugin will write `local.properties` on first Gradle sync if it is missing.

Connect a device or emulator, then:

```bash
./gradlew :game:hermesRunAndroid
```

## Configuration

| File | Purpose |
|------|---------|
| [`game/hermes.json`](game/hermes.json) | Game data: **`title`** (window / tab / app label), **`scene`** path |
| [`settings.gradle`](settings.gradle) | **`hermes { platforms { … } }`** — enable platforms (`desktop` / `html` / `android`) |
| [`game/build.gradle`](game/build.gradle) | **`version`**, `hermes { applicationClass, debug, assetsDirectory, icons, platforms { … } }` |
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

### `game/build.gradle` (build / run / export options)

| Platform | Settings |
|----------|----------|
| **desktop** | `width`, `height`, `vsync`, `resizable`, `foregroundFps`, `bundleId`, `executableName`, `exportTargets` |
| **html** | `width`, `height`, `devServerPort`, `webAssembly` |
| **android** | `applicationId`, `minSdk`, `targetSdk`, `compileSdk`, `versionCode`, `screenOrientation` |

Artifact **version** for stores and Android `versionName` comes from `version = '…'` in `game/build.gradle`, not from `hermes.json`.

## Exporting

Build distribution ZIPs for enabled platforms:

```bash
./gradlew :game:hermesExport          # all enabled platforms
./gradlew :game:hermesExportDesktop   # native desktop bundles
./gradlew :game:hermesExportHtml      # static HTML/WASM site
./gradlew :game:hermesExportAndroid   # release APK (unsigned)
```

| Task | Output |
|------|--------|
| `hermesExportDesktop` | `game/build/dist/desktop/*-{linux-x64,macos-aarch64,macos-x64,windows-x64}.zip` |
| `hermesExportHtml` | `game/build/dist/html/*-html.zip` |
| `hermesExportAndroid` | `game/build/dist/android/*-android.zip` |

Desktop exports build **host-runnable** targets only (jlink is not cross-compiled). On macOS you get `macos-aarch64` and/or `macos-x64`; on Linux, `linux-x64`; on Windows, `windows-x64`.

### Icons

Replace files under `game/src/main/resources/assets/icons/` (paths relative to `assetsDirectory`, default `src/main/resources/assets`):

| File | Platform |
|------|----------|
| `icons/desktop/mac.icns` | macOS `.app` (Construo) |
| `icons/desktop/windows.png` | Windows `.exe` |
| `icons/android/ic_launcher.png` | Android launcher (512×512 PNG recommended) |
| `icons/web/favicon.png` | HTML export favicon |

Re-run the relevant `hermesExport*` task after changing icons.

### Running exported builds

**Desktop (macOS):** unzip the ZIP, then open the `.app` inside. If macOS says the app “can’t be opened”, try:

- Right-click the `.app` → **Open** → **Open** (first launch for unsigned apps), or
- In Terminal: `xattr -cr /path/to/my-game.app` then double-click again.

**HTML:** unzip the ZIP and run `./serve.sh` from the export folder (or `serve.bat` on Windows). Open the printed `http://127.0.0.1:8080/` URL. Do **not** open `webapp/index.html` via `file://` — browsers block WebAssembly loads from local files.

### Other useful tasks

| Task | Description |
|------|-------------|
| `hermesDoctor` | Validate setup, JDK, SDK, forbidden libGDX imports in game code |
| `validateHermesJson` | Parse and validate `hermes.json` |
| `hermesSyncPlatforms` | Refresh launcher stubs under `.hermes/platforms/` |

Root task: `./gradlew hermesSyncPlatforms` (same sync, run from project root).

## Custom components

This template registers `PulseMarker` via `META-INF/services/dev.hermes.api.ecs.ComponentRegistration`.

Scene format: in the Hermes engine repository, see `docs/scene-format-v1.md` (same path as in this monorepo checkout).

Example scene: [`game/src/main/resources/assets/scenes/main.json`](game/src/main/resources/assets/scenes/main.json).

## Troubleshooting

- **Plugins not found:** run `publishToMavenLocal` from the Hermes engine checkout; check `hermes.engineVersion` in `gradle.properties`.
- **Android SDK not found:** create `local.properties` with `sdk.dir=…` or pass `--android-sdk` when running `hermes new`.
- **HTML/Android task missing:** enable the platform in `settings.gradle`, sync Gradle, and run `hermesDoctor`.
- **Export fails after engine upgrade:** run `./gradlew hermesSyncPlatforms` to refresh `.hermes/platforms/`.
