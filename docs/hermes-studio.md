# Hermes Studio

Hermes Studio is a local dev tool for Hermes game projects: live inspector (HDP), unified project config, project file tree, and play mode that runs `:game:hermesRunDesktop` with logs.

## Launch

### CLI (recommended for standalone games)

From a project with `game/hermes.json`:

```bash
hermes studio .
# or
hermes studio /path/to/my-game
```

Resolution order:

1. `HERMES_HOME` or a parent directory that contains the Hermes engine checkout (`gradlew` + `settings.gradle` with `dev.hermes.settings`)
2. Run `./gradlew :hermes-studio-app:run --args="--project <dir>"` from the engine repo
3. Or run `java -jar <engine>/studio/hermes-studio.jar` if a fat jar is installed

### VS Code / Cursor

Install or open the workspace extension at [`extensions/hermes-vscode/`](../extensions/hermes-vscode/):

| Command | Action |
|---------|--------|
| **Hermes: Run Desktop** | `./gradlew :game:hermesRunDesktop` in a terminal |
| **Hermes: Open Inspector** | Webview with the bundled Studio UI; connects to HDP on port `18765` |

Activation: workspace contains `game/hermes.json`.

### Engine development

```bash
./gradlew :hermes-studio-app:run --args="--project ."
```

Rebuild UI after changes: `cd hermes-studio-ui && npm run build`, then refresh classpath resources / extension `media/`.

## Unified Project Config panel

One **Project Config** panel edits settings from three sources without separate file tabs:

| Section | Source file | Examples |
|---------|-------------|----------|
| Game | `game/hermes.json` | `title`, `scene` |
| Project | `game/build.gradle` `hermes { }` | `applicationClass`, `assetsDirectory`, `debug`, `version` |
| Platforms | `settings.gradle` `hermes { platforms { } }` | enable desktop / html / android, window size |

REST API (Jetty, default http://localhost:8765):

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/config` | GET | Full `HermesProjectConfigView` JSON |
| `/api/config` | PATCH | Partial update; saves to the correct file per field |

The UI shows **Save** / **Revert** and a dirty indicator when the draft differs from the last GET. Each section includes `sourceFile` for hover hints.

Implementation: `ConfigAggregator` in `hermes-studio-core`, `configPanel.ts` in `hermes-studio-ui`.

## Play mode

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/play/run` | POST | Start `./gradlew :game:hermesRunDesktop` |
| `/api/play/stop` | POST | Stop the Gradle process |
| `/api/play/port` | GET | HDP port (`18765`) |

The Studio UI **Play** button calls these endpoints, then connects to HDP. Run the game with `debug = true` in `game/build.gradle` so the debug server listens.

## Project files panel

The **Files** panel lists the project tree via `GET /api/files/tree`. Double-click a file sends `POST /api/files/open` with `{ "path": "relative/or/abs" }`; the server opens the file in the OS default application (typically your IDE). Requires a project to be open (`POST /api/project/open` with `{ "path": "..." }` — the CLI passes `--project` on startup).

## Export safety

Distribution tasks (`hermesExportDesktop`, `hermesExport`, …) force `hermes.debug=false` regardless of the `debug` flag in `build.gradle`. The HDP WebSocket server does not start in exported builds, so shipped games do not listen on port `18765`.

Verify in tests: `ExportSafetyTest` in `hermes-core`.

## Hermes Debug Protocol

Studio and the inspector webview speak [HDP v0](hermes-debug-protocol-v0.md) to the running game. Custom component fields require registration — see [component-inspector-registration.md](component-inspector-registration.md).
