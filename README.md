# Hermes

Hermes is a Java-first game engine layered on [libGDX](https://libgdx.com/). User-facing code targets a small public API (`hermes-api`); libGDX stays inside `hermes-core` and launcher modules.

**Vision and requirements** (longer form): see [plan](plan/).

---

## Phase 0 — build and run (desktop)

### Prerequisites

- **JDK 11** or newer (toolchain targets Java 11).
- Network access the first time Gradle resolves dependencies.

### Clone and run

```bash
./gradlew :hermes-launcher-desktop:run
```

The Gradle `run` task uses the repo root `assets/` directory as the working directory (same pattern as the gdx-liftoff template). You should see a 640×480 window with the sample libGDX logo; the internal `:sample-game` module implements `HermesApplication` with **no** `com.badlogicgames.gdx` imports.

Cold-cache sanity check:

```bash
./gradlew clean build
```

### Module graph (Phase 0)

- `sample-game` → `hermes-api` (compile only; no libGDX).
- `hermes-core` → `hermes-api` (api) + `gdx` (implementation).
- `hermes-launcher-desktop` → `hermes-core`, `sample-game`, LWJGL3 backend + desktop natives.

### Template provenance

Desktop launcher behavior (Gradle JVM args, `StartupHelper`, LWJGL version constraints) is adapted from the local **gdx-liftoff** template:

`/Users/assafa/Documents/hermes/untitled folder/template`

Versions are aligned with that template where it matters (e.g. **libGDX 1.14.0**, **LWJGL 3.4.1**). `StartupHelper` is included under `hermes-launcher-desktop` with its original license header (Apache 2.0, damios).

### Troubleshooting

- **macOS:** LWJGL3 normally requires `-XstartOnFirstThread`. `StartupHelper` restarts the JVM with that flag when needed; the `run` task also adds it on macOS.
- **Linux + NVIDIA:** Debugging LWJGL can require `__GL_THREADED_OPTIMIZATIONS=0`. The `run` task sets this for Gradle-spawned JVMs (see template comments in `hermes-launcher-desktop/build.gradle`).

### Useful Gradle commands

```bash
./gradlew projects
./gradlew :hermes-api:dependencies --configuration compileClasspath
```

Verify the sample has no direct libGDX imports:

```bash
rg 'com\.badlogicgames\.gdx' sample-game/src
```

(expected: no matches)

### CI and releases

- **CI:** [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs `./gradlew clean build` on **every push** (any branch) and **every pull request** (JDK 11 and 17 on Ubuntu).
- **Releases:** [`.github/workflows/release.yml`](.github/workflows/release.yml) builds artifacts on tag `v*` or manual dispatch; tag pushes also create a GitHub Release with **auto-generated notes** driven by [`.github/release.yml`](.github/release.yml). Label PRs (for example `feature`, `bug`, `documentation`) so notes are grouped; use `ignore-for-release` or `skip-changelog` to omit a PR from the changelog.

### References

- [libGDX wiki](https://libgdx.com/wiki/)
- [libGDX Javadoc](https://javadoc.io/doc/com.badlogicgames.gdx)
