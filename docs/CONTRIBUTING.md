# Contributing to Hermes

## Prerequisites

- **JDK 17** for local development aligned with CI (`.github/workflows/ci.yml`). Subprojects compile with **Java 11** bytecode (`options.release = 11`); the toolchain may use a newer JDK to compile.
- **Gradle** — use the wrapper: `./gradlew …`
- **Android** (optional): SDK via `local.properties` (`sdk.dir`), `hermes.android.sdk`, or `ANDROID_SDK_ROOT` / `ANDROID_HOME`. See `local.properties.example` and README § Android SDK.

## Build and test

Full verification (matches CI core job):

```bash
./gradlew clean build publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal :hermes-gradle-plugin:test :hermes-cli:test
```

Cold-cache smoke:

```bash
./gradlew clean build
```

### Per-module tests

| Module | Command |
|--------|---------|
| API (if tests added) | `./gradlew :hermes-api:test` |
| Core / ECS / scenes | `./gradlew :hermes-core:test` |
| Tooling | `./gradlew :hermes-gradle-plugin:hermes-tooling:test` (alias: `./gradlew hermes-tooling-test`) |
| Gradle plugin | `./gradlew :hermes-gradle-plugin:test` |
| CLI | `./gradlew :hermes-cli:test` |
| Game (no unit tests by default) | `./gradlew :game:hermesDoctor` |

Run a single test class:

```bash
./gradlew :hermes-core:test --tests 'dev.hermes.core.ecs.SceneParserTest'
```

## Doctor and export smoke

**Doctor** — forbidden libGDX imports, engine resolution, JDK/SDK hints:

```bash
./gradlew :game:hermesDoctor
```

From a template project: same task on `:game`, or `hermes doctor` after installing the CLI.

**Export smoke** (CI runs these on push; optional locally):

```bash
./gradlew :game:hermesExportHtml
./gradlew :game:hermesExportAndroid    # requires Android SDK
./gradlew :game:hermesExportDesktop
```

Validate ZIP layout with `.github/scripts/validate-export-zip.sh` when debugging export regressions.

## Dogfood desktop run

**Manual** — opens the sample window:

```bash
./gradlew :game:hermesRunDesktop
```

Expect a 640×480 window with the sample scene. Game class is set via `hermes { applicationClass = '…' }` in `game/build.gradle`.

**Automated smoke (Phase 2b)** — headless-friendly run with a frame cap (not yet in all branches; documented for upcoming CI):

```bash
./gradlew :game:hermesRunDesktop -Phermes.desktop.smokeFrames=2
```

Use this in scripts/CI to verify startup without a human closing the window. Until Phase 2b lands in your branch, prefer `:game:hermesDoctor` and `:hermes-core:test` for gate checks.

## CLI development

```bash
./gradlew :hermes-cli:installDist
export PATH="$PWD/hermes-cli/build/install/hermes/bin:$PATH"
hermes new /tmp/my-game --name MyGame --package dev.hermes.mygame
cd /tmp/my-game
hermes doctor
```

Templates require `publishToMavenLocal` from the engine repo first (see [ARCHITECTURE.md](ARCHITECTURE.md)).

## Publishing locally

Before testing `hermes new` or a template checkout:

```bash
./gradlew publishToMavenLocal :hermes-gradle-plugin:publishToMavenLocal
```

### Standalone `hermes-gradle-plugin` development

Opening or building only `hermes-gradle-plugin/` (outside the monorepo root) resolves `dev.hermes:hermes-tooling` from Maven local, not a composite `project(':hermes-tooling')`. Publish tooling first:

```bash
./gradlew publishToMavenLocal
# or from the engine repo root:
./gradlew :hermes-tooling:publishToMavenLocal
```

In the monorepo, `hermes-tooling` is included only once (under the composite `hermes-gradle-plugin` build). Root modules such as `hermes-cli` resolve the published `dev.hermes:hermes-tooling` artifact from Maven local after `:hermes-gradle-plugin:hermes-tooling:publishToMavenLocal`. Standalone plugin checkouts use the same `include 'hermes-tooling'` in `hermes-gradle-plugin/settings.gradle` and must run `publishToMavenLocal` for tooling first.

## CI reference

- **Build:** `.github/workflows/ci.yml` — matrix `ubuntu`, `windows`, `macos`, JDK 17.
- **Exports:** separate jobs for HTML, Android, and desktop per OS after the main build.

## Docs changes

- User-facing guides: `docs/` (index: [README.md](README.md)).
- Root `README.md` — onboarding and phase-oriented how-tos.
- Do not commit `local.properties` or secrets.

## Code conventions

- No libGDX imports under `game/src/`.
- Public API changes belong in `hermes-api`; implementation in `hermes-core`.
- Shared non-Gradle logic belongs in `hermes-tooling` (see [ARCHITECTURE.md](ARCHITECTURE.md)).
