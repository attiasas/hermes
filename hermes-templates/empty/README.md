# {{PROJECT_NAME}}

Hermes empty game template.

## Prerequisites

- JDK 11+
- Hermes engine artifacts and Gradle plugin in Maven local

From the Hermes engine repository:

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

This project contains only the **`game`** module. The desktop launcher lives under `.hermes/platforms/` (not the full engine tree).

## Run (desktop)

```bash
./gradlew :game:hermesRunDesktop
```

## Validate

```bash
./gradlew :game:hermesDoctor
```

## Configuration

| File | Purpose |
|------|---------|
| `game/hermes.json` | Game data: `name`, `version`, `scene` path |
| `settings.gradle` | Enabled platforms (desktop / html / android) |
| `game/build.gradle` | `applicationClass`, window size, debug flag |
| `gradle.properties` | `hermes.engineVersion`, libGDX versions (`gdxVersion`, `lwjgl3Version`, …) |

## Custom components

This template includes `PulseMarker` registered via `META-INF/services/dev.hermes.api.ecs.ComponentRegistration`.
See [scene-format-v1.md](https://github.com/your-org/hermes/blob/main/docs/scene-format-v1.md) in the engine repo.

Scene example: `game/src/main/resources/assets/scenes/main.json`.
