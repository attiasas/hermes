# {{PROJECT_NAME}}

Hermes game project generated from the **multi-scene** template.

Demonstrates scene registration, `SceneChangeRequest.push` / `pop`, and a timer-driven pause overlay. See the Hermes engine docs: `docs/scene-management.md`.

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
./gradlew :game:hermesRunDesktop
```

The pause overlay appears every five seconds while the main scene keeps running underneath.

## Running

### Desktop

Requires `desktop { enabled = true }` in `settings.gradle`.

```bash
./gradlew :game:hermesRunDesktop
```

### HTML (TeaVM)

Requires `html { enabled = true }` in `settings.gradle`.

```bash
./gradlew :game:hermesRunHtml
```

### Android

Requires `android { enabled = true }` in `settings.gradle` and a valid SDK.

```bash
./gradlew :game:hermesRunAndroid
```

## Configuration

| File | Purpose |
|------|---------|
| [`game/hermes.json`](game/hermes.json) | Game data: **`title`**, bootstrap **`scene`** path |
| [`settings.gradle`](settings.gradle) | **`hermes { platforms { … } }`** — enable platforms |
| [`game/build.gradle`](game/build.gradle) | **`version`**, `hermes { applicationClass, debug, assetsDirectory, icons, platforms { … } }` |

## Custom components

`PulseMarker` registers via `META-INF/services/dev.hermes.api.ecs.ComponentRegistration`. Scene JSON lives under `game/src/main/resources/assets/scenes/`.

## Troubleshooting

- **Plugins not found:** run `publishToMavenLocal` from the Hermes engine checkout; check `hermes.engineVersion` in `gradle.properties`.
- **Export fails after engine upgrade:** run `./gradlew hermesSyncPlatforms`.
