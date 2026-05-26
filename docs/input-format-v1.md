# Input profile format v1

Input profiles describe remapped **actions** and device **bindings**. They live under the game assets directory and are
referenced from `hermes.json` via `inputProfile` (required).

Hermes is **pre-release**; the profile format stays **v1** until 1.0. Update this doc in the same PR when JSON rules
change.

## Top-level shape

```json
{
  "version": 1,
  "context": "gameplay",
  "actions": {
    "move_x": { "type": "axis" },
    "move_y": { "type": "axis" },
    "select": { "type": "button" },
    "pause": { "type": "button" }
  },
  "bindings": [
    { "action": "move_x", "source": "keyboard", "key": "D", "scale": 1 },
    { "action": "move_x", "source": "keyboard", "key": "A", "scale": -1 },
    { "action": "select", "source": "pointer", "button": "LEFT", "when": "justPressed" },
    { "action": "pause", "source": "keyboard", "key": "ESCAPE", "when": "justPressed" },
    { "action": "move_x", "source": "gamepad", "axis": "LEFT_X" }
  ],
  "gamepad": { "deadzone": 0.15 }
}
```

## Fields

| Field       | Required | Description |
|-------------|----------|-------------|
| `version`   | Yes      | Must be `1` (integer ≥ 1). |
| `context`   | Yes      | Default action context id when the active scene has no `inputContext`. |
| `actions`   | Yes      | Map of action name → `{ "type": "button" \| "axis" }`. Must not be empty. |
| `bindings`  | Yes      | Array of binding objects (see below). |
| `gamepad`   | No       | Object with optional `deadzone` (float, default `0.15`). Applied to gamepad axis reads. |

Unknown top-level keys are ignored. Parse errors throw `InputProfileParseException` at load time.

## Actions

Each entry in `actions` names a logical control:

| `type`   | Runtime API |
|----------|-------------|
| `button` | `pressed`, `justPressed`, `justReleased` |
| `axis`   | `axis` (scalar −1..1), `axis2` (2D, future use) |

Action names are referenced by `bindings[].action`. Every binding must reference a defined action.

## Bindings

Each binding object maps one device input to one action.

| Field     | Required | Description |
|-----------|----------|-------------|
| `action`  | Yes      | Name from `actions`. |
| `source`  | Yes      | `"keyboard"`, `"pointer"`, or `"gamepad"`. |
| `when`    | No       | `"pressed"` (default), `"justPressed"`, `"justReleased"`. Applies to keyboard keys, pointer buttons, and gamepad buttons. |
| `context` | No       | When set, binding applies only when `actions.context()` equals this string. Omit or use `"*"` for all contexts. |
| `scale`   | No       | Float multiplier (default `1`). For keyboard: applied when key is held. For gamepad axis: multiplied after deadzone. |

### Source: keyboard

| Field | Required | Description |
|-------|----------|-------------|
| `key` | Yes      | Key name (case-insensitive). See [registered keys](#registered-key-names). |

Example:

```json
{ "action": "pause", "source": "keyboard", "key": "ESCAPE", "when": "justPressed" }
```

### Source: pointer

| Field    | Required | Description |
|----------|----------|-------------|
| `button` | Yes      | `"LEFT"`, `"RIGHT"`, or `"MIDDLE"`. |

Example:

```json
{ "action": "select", "source": "pointer", "button": "LEFT", "when": "justPressed" }
```

### Source: gamepad

Provide **either** `axis` **or** `button` (not both).

| Field    | Required | Description |
|----------|----------|-------------|
| `axis`   | One of   | Axis name for axis-type actions. See [registered axes](#registered-gamepad-axes). |
| `button` | One of   | Button name for button-type actions. See [registered gamepad buttons](#registered-gamepad-buttons). |

Example:

```json
{ "action": "move_x", "source": "gamepad", "axis": "LEFT_X" }
```

## Context resolution

1. Profile `context` is the default (e.g. `"gameplay"`).
2. Active scene may override with top-level `inputContext` in scene JSON (see [scene-format-v1.md](scene-format-v1.md)).
3. Each frame, `InputActions.context()` returns the active value.
4. Bindings with `context` set run only when it matches; others are global.

Scene overlays (push/pop) switch context when the top scene changes.

## Axis combining

Multiple bindings may target the same axis action (e.g. WASD + stick). Contributions are summed per frame, then clamped to
`[-1, 1]`.

## Registered key names

Parsed via `InputKey.byName` (libGDX `Input.Keys` values):

`A`, `D`, `S`, `W`, `SPACE`, `F1`, `ESCAPE`

Additional keys can be added to `hermes-api` as the engine grows; until then, use `devices().keyboard()` with raw key
codes for unlisted keys.

## Registered pointer buttons

`LEFT`, `RIGHT`, `MIDDLE` (`InputButton`).

## Registered gamepad axes

`LEFT_X`, `LEFT_Y`, `RIGHT_X`, `RIGHT_Y`, `LEFT_TRIGGER`, `RIGHT_TRIGGER` (`GamepadAxis`).

## Registered gamepad buttons

`A`, `B`, `X`, `Y`, `L1`, `R1`, `START`, `SELECT` (`GamepadButton`).

## Validation

Doctor and `HermesGameConfigParser` require `inputProfile` in `hermes.json`. The engine loads the asset at startup; missing
files or invalid JSON fail before the first frame.

## Related docs

- [Input system overview](input.md)
- [Scene format v1](scene-format-v1.md)
- [Runtime config](runtime-config.md)
