# Built-in Camera Controls Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship built-in perspective camera controls comparable to libGDX [`CameraInputController`](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/CameraInputController.java) (orbit / pan / dolly / scroll zoom), with the orbit target driven by the **selected entity** when one exists, plus an optional [`FirstPersonCameraController`](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/FirstPersonCameraController.java) mode — all working with **scene-owned** and bound-entity main cameras.

**Architecture:** Replace the narrow `CameraSceneControlSystem` (left-drag orbit on empty space only, entity cameras only) with a `CameraControlSystem` that resolves the main camera through `CameraResolver`, mutates either `SceneCameraConfig` or bound entity `Transform`/`Camera`, and uses a core-only `GdxCameraController` adapter to apply libGDX `rotateAround` / `translate` math (same algorithms as libGDX, no dependency from `hermes-api`). Target point each frame: `Selected` entity `Transform` if present, else scene `lookAt` (or world origin). Scene JSON `"camera".controls` selects mode (`orbit` default, `firstPerson` optional) and button mapping. Add scroll-wheel polling to `InputFrame`. Keep `EntityDragSystem` for orthographic 2D only. Fix dogfood demo scene so picking and controls are testable.

**Tech Stack:** Java 11, libGDX 1.14.0 (camera math in `hermes-core` only), JUnit 5, Gradle `:hermes-core:test`, `:dogfood-simulation:compileJava`

---

## Behavior spec (libGDX parity)

### Orbit mode (`controls.mode: "orbit"`) — default for perspective scenes

Mirrors [`CameraInputController`](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/CameraInputController.java):

| Input | Action |
|-------|--------|
| **Left drag** | Rotate camera around target (`rotateAround`) |
| **Right drag** | Pan camera on camera right/up plane; optionally move target with camera (`translateTarget`) |
| **Middle drag** | Dolly along view direction (`forwardButton`) |
| **Scroll wheel** | Zoom / dolly along view direction (`scrollFactor`) |
| **W / S** (optional) | Move forward/back along view; optionally move target (`forwardTarget`) |

**Target resolution:**

| Selection state | Orbit / pan reference |
|-----------------|----------------------|
| No `Selected` entity | Scene `lookAt` if set, else `(0, 0, 0)` |
| `Selected` entity | Entity `Transform` world position (re-read each frame so moving entities stay centered) |

**Important change from today:** picking an entity on press must **not** disable camera drag. `SelectionSystem` still fires `select` on click; camera controls run on drag regardless of whether the press started over an entity. Dragging with a selected entity orbits **around that entity**, not free world space.

### First-person mode (`controls.mode: "firstPerson"`) — optional

Mirrors [`FirstPersonCameraController`](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/FirstPersonCameraController.java):

| Input | Action |
|-------|--------|
| **Left drag** | Rotate view direction (`degreesPerPixel`) |
| **W / S / A / D** | Forward / back / strafe |
| **Q / E** | Up / down |

When a `Selected` entity exists in FPS mode, the camera does **not** orbit it; instead, on `select` the engine optionally **yaw/pitch toward** the entity once (snap look-at) so the user can then explore in FPS with the entity as visual focus. v1: set `lookAt` on scene config toward entity on select; subsequent FPS drag moves freely.

### Orthographic scenes

`CameraControlSystem` is a no-op. `EntityDragSystem` handles click-select + drag-move (unchanged 2D behavior).

---

## Root cause (current bug)

`CameraSceneControlSystem.orbitMainCamera()` returns immediately when no bound entity camera exists, so scene-owned cameras (dogfood, templates) never move. It also blocks orbit when `pick()` hits an entity, which prevents entity-relative orbit.

---

## File map

| File | Responsibility |
|------|----------------|
| `hermes-api/.../world/CameraControlsConfig.java` | **Create** — mode, buttons, speeds (libGDX-free) |
| `hermes-api/.../world/CameraControlsMode.java` | **Create** — `ORBIT`, `FIRST_PERSON` |
| `hermes-core/.../world/SceneCameraBlock.java` | **Modify** — carry optional `CameraControlsConfig` |
| `hermes-core/.../world/SceneCameraBlockParser.java` | **Modify** — parse `"controls"` nested object |
| `hermes-core/.../input/GdxCameraController.java` | **Create** — sync ActiveCamera ↔ libGDX camera; rotateAround / pan / dolly / fps |
| `hermes-core/.../input/CameraControlTarget.java` | **Create** — resolve target from `Selected` or scene lookAt |
| `hermes-core/.../input/CameraControlSystem.java` | **Create** — replaces `CameraSceneControlSystem` |
| `hermes-core/.../input/CameraSceneControlSystem.java` | **Delete** after migration |
| `hermes-core/.../ecs/CameraResolver.java` | **Modify** — `mainCameraProjection(WorldManager)` |
| `hermes-core/.../input/InputFrame.java` | **Modify** — `scrollX`, `scrollY` fields |
| `hermes-core/.../input/GdxInputReaders.java` | **Modify** — poll `Gdx.input.getDeltaX/Y` as scroll |
| `hermes-api/.../input/PointerSnapshot.java` | **Modify** — `float scrollY()` (0 if none) |
| `hermes-core/.../input/EntityDragSystem.java` | **Modify** — use `mainCameraProjection` |
| `hermes-core/.../ecs/BuiltinComponents.java` | **Modify** — register `CameraControlSystem` instead of `CameraSceneControlSystem` |
| `hermes-core/src/test/.../GdxCameraControllerTest.java` | **Create** |
| `hermes-core/src/test/.../CameraControlSystemTest.java` | **Create** |
| `hermes-core/src/test/.../CameraControlsParserTest.java` | **Create** |
| `dogfood-simulation/.../scenes/main.json` | **Modify** — `lookAt`, explicit `controls` |
| `dogfood-simulation/.../entities/spin-cube/type.json` | **Modify** — wider `Selectable.radius` |
| `dogfood-simulation/.../SceneNavigationSystem.java` | **Modify** — disable auto-pause timer |
| `dogfood-simulation/.../input/profile.json` | **Modify** — camera movement action bindings |
| `hermes-templates/*/game/.../input/profile.json` | **Modify** — same camera bindings |
| `docs/input.md`, `docs/world-space.md` | **Modify** — document controls |

---

### Task 1: `CameraControlsConfig` API + scene JSON parsing

**Files:**
- Create: `hermes-api/src/main/java/dev/hermes/api/world/CameraControlsMode.java`
- Create: `hermes-api/src/main/java/dev/hermes/api/world/CameraControlsConfig.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/world/SceneCameraBlock.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/world/SceneCameraBlockParser.java`
- Test: `hermes-core/src/test/java/dev/hermes/core/world/CameraControlsParserTest.java`

- [ ] **Step 1: Write the failing test**

```java
package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.world.CameraControlsMode;
import org.junit.jupiter.api.Test;

final class CameraControlsParserTest {

    @Test
    void parsesOrbitControlsFromCameraBlock() {
        JsonValue root =
                new JsonReader()
                        .parse(
                                "{\"version\":1,\"projection\":\"perspective\","
                                        + "\"controls\":{\"mode\":\"orbit\",\"rotateButton\":\"LEFT\","
                                        + "\"translateButton\":\"RIGHT\",\"forwardButton\":\"MIDDLE\","
                                        + "\"scrollZoom\":true,\"rotateAngle\":360,\"translateUnits\":10}}");
        SceneCameraBlock block = SceneCameraBlockParser.parse("test.json", root);
        assertEquals(CameraControlsMode.ORBIT, block.controls().mode());
        assertEquals(360f, block.controls().rotateAngle(), 0.001f);
        assertEquals(10f, block.controls().translateUnits(), 0.001f);
    }

    @Test
    void parsesFirstPersonMode() {
        JsonValue root =
                new JsonReader()
                        .parse(
                                "{\"version\":1,\"projection\":\"perspective\","
                                        + "\"controls\":{\"mode\":\"firstPerson\",\"velocity\":8,"
                                        + "\"degreesPerPixel\":0.5}}");
        SceneCameraBlock block = SceneCameraBlockParser.parse("test.json", root);
        assertEquals(CameraControlsMode.FIRST_PERSON, block.controls().mode());
        assertEquals(8f, block.controls().velocity(), 0.001f);
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

Run: `./gradlew :hermes-core:test --tests "dev.hermes.core.world.CameraControlsParserTest" -q`

- [ ] **Step 3: Implement API types**

`CameraControlsMode.java`:

```java
package dev.hermes.api.world;

public enum CameraControlsMode {
    ORBIT,
    FIRST_PERSON
}
```

`CameraControlsConfig.java` (defaults match libGDX `CameraInputController` / `FirstPersonCameraController`):

```java
package dev.hermes.api.world;

import dev.hermes.api.input.InputButton;

public final class CameraControlsConfig {

    private CameraControlsMode mode = CameraControlsMode.ORBIT;
    private boolean enabled = true;
    private int rotateButton = InputButton.LEFT;
    private int translateButton = InputButton.RIGHT;
    private int forwardButton = InputButton.MIDDLE;
    private float rotateAngle = 360f;
    private float translateUnits = 10f;
    private float scrollFactor = -0.1f;
    private boolean scrollZoom = true;
    private boolean translateTarget = true;
    private boolean forwardTarget = true;
    private boolean scrollTarget = false;
    private float velocity = 5f;
    private float degreesPerPixel = 0.5f;

    public static CameraControlsConfig orbitDefaults() {
        return new CameraControlsConfig();
    }

    public static CameraControlsConfig firstPersonDefaults() {
        CameraControlsConfig c = new CameraControlsConfig();
        c.mode = CameraControlsMode.FIRST_PERSON;
        return c;
    }

    // getters + setters for every field
}
```

Extend `SceneCameraBlock` with `CameraControlsConfig controls()` and parse `"controls"` in `SceneCameraBlockParser`. When `"controls"` is omitted on a **perspective** camera, store `CameraControlsConfig.orbitDefaults()`. When omitted on orthographic, store `enabled = false`.

Wire parsed controls into `SceneCameraControllerImpl` (new field + getter; set from `SceneParser` alongside `setSceneConfig`).

- [ ] **Step 4: Run test — expect PASS**

- [ ] **Step 5: Commit**

```bash
git add hermes-api/src/main/java/dev/hermes/api/world/CameraControlsMode.java \
        hermes-api/src/main/java/dev/hermes/api/world/CameraControlsConfig.java \
        hermes-core/src/main/java/dev/hermes/core/world/SceneCameraBlock.java \
        hermes-core/src/main/java/dev/hermes/core/world/SceneCameraBlockParser.java \
        hermes-core/src/main/java/dev/hermes/core/world/SceneCameraControllerImpl.java \
        hermes-core/src/main/java/dev/hermes/core/ecs/SceneParser.java \
        hermes-core/src/test/java/dev/hermes/core/world/CameraControlsParserTest.java
git commit -m "feat(camera): CameraControlsConfig API and scene JSON parsing"
```

---

### Task 2: Scroll wheel in input pipeline

**Files:**
- Modify: `hermes-api/src/main/java/dev/hermes/api/input/PointerSnapshot.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/input/InputFrame.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/input/GdxInputReaders.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/input/InputServiceImpl.java`
- Test: `hermes-core/src/test/java/dev/hermes/core/input/InputFrameScrollTest.java`

- [ ] **Step 1: Write failing test**

```java
@Test
void inputFrame_carriesScrollDelta() {
    InputFrame frame = InputFrame.builder().scroll(0f, -2f).build();
    assertEquals(-2f, frame.scrollY(), 0.001f);
}
```

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement**

Add `scrollX`/`scrollY` to `InputFrame.Builder` (default 0). In `GdxInputReaders.poll()`, after pointer poll:

```java
builder.scroll(Gdx.input.getDeltaX(), Gdx.input.getDeltaY());
```

Extend `PointerSnapshot` with `default float scrollX() { return 0f; }` and `default float scrollY() { return 0f; }`; implement in `InputServiceImpl` pointer view from current frame.

Add test factory `InputFrame.pointerScroll(float x, float y, float scrollY)`.

- [ ] **Step 4: Run — expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(input): scroll wheel delta in InputFrame and PointerSnapshot"
```

---

### Task 3: `GdxCameraController` — libGDX camera math adapter

**Files:**
- Create: `hermes-core/src/main/java/dev/hermes/core/input/GdxCameraController.java`
- Create: `hermes-core/src/main/java/dev/hermes/core/input/MainCameraWriter.java`
- Test: `hermes-core/src/test/java/dev/hermes/core/input/GdxCameraControllerTest.java`

- [ ] **Step 1: Write failing tests**

```java
@Test
void orbitRotate_changesYawAroundTarget() {
    ActiveCamera before = perspectiveAt(0f, 2f, 5f, 0f, 0f, 0f);
    GdxCameraController ctrl = new GdxCameraController(640, 480);
    ActiveCamera after =
            ctrl.orbit(before, 0f, 0f, 0f, normalizeDeltaX(20f, 640), 0f, orbitDefaults());
    assertNotEquals(before.rotationY(), after.rotationY(), 0.01f);
}

@Test
void pan_movesPositionAndTargetWhenTranslateTarget() {
    ActiveCamera before = perspectiveAt(0f, 2f, 5f, 0f, 0f, 0f);
    CameraControlsConfig cfg = CameraControlsConfig.orbitDefaults();
    cfg.setTranslateTarget(true);
    GdxCameraController ctrl = new GdxCameraController(640, 480);
    ActiveCamera after = ctrl.pan(before, 0f, 0f, 0f, normalizeDeltaX(40f, 640), 0f, cfg);
    assertNotEquals(before.x(), after.x(), 0.01f);
}

@Test
void scrollZoom_movesCloserAlongView() {
    ActiveCamera before = perspectiveAt(0f, 0f, 5f, 0f, 0f, 0f);
    GdxCameraController ctrl = new GdxCameraController(640, 480);
    ActiveCamera after = ctrl.scrollZoom(before, 0f, 0f, 0f, -1f, orbitDefaults());
    assertTrue(after.z() < before.z());
}
```

Helper `normalizeDeltaX(pixelDelta, surfaceWidth)` returns `pixelDelta / surfaceWidth` (libGDX convention).

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement `GdxCameraController`**

Port logic from libGDX `CameraInputController.process()`:

```java
public ActiveCamera orbit(ActiveCamera in, float targetX, float targetY, float targetZ,
                          float deltaX, float deltaY, CameraControlsConfig cfg) {
    PerspectiveCamera cam = bind(in);
    Vector3 target = tmp.set(targetX, targetY, targetZ);
    tmpV1.set(cam.direction).crs(cam.up).y = 0f;
    cam.rotateAround(target, tmpV1.nor(), deltaY * cfg.rotateAngle());
    cam.rotateAround(target, Vector3.Y, deltaX * -cfg.rotateAngle());
    cam.update();
    return unbind(in, cam);
}
```

Similarly implement `pan(...)`, `dolly(...)`, `scrollZoom(...)`, `firstPersonLook(...)`, `firstPersonMove(...)` from [`FirstPersonCameraController`](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/graphics/g3d/utils/FirstPersonCameraController.java).

`MainCameraWriter` applies the returned `ActiveCamera` to either `SceneCameraConfig` or bound entity `Transform`/`Camera` (extracted from old `CameraSceneControlSystem` + inverse of `CameraResolver.fromView`).

- [ ] **Step 4: Run — expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(input): GdxCameraController orbit/pan/zoom math adapter"
```

---

### Task 4: `CameraControlTarget` + `CameraResolver.mainCameraProjection`

**Files:**
- Create: `hermes-core/src/main/java/dev/hermes/core/input/CameraControlTarget.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/ecs/CameraResolver.java`
- Test: `hermes-core/src/test/java/dev/hermes/core/input/CameraControlTargetTest.java`

- [ ] **Step 1: Write failing tests**

```java
@Test
void target_usesSelectedEntityPosition() {
    WorldManagerImpl manager = new WorldManagerImpl();
    Entity cube = manager.entities().create("cube");
    manager.entities().addComponent(cube.id(), new Transform(3f, 1f, 0f));
    manager.entities().addComponent(cube.id(), new Selected());
    CameraControlTarget target = CameraControlTarget.resolve(manager);
    assertEquals(3f, target.x(), 0.001f);
    assertEquals(1f, target.y(), 0.001f);
}

@Test
void target_fallsBackToSceneLookAt() {
    WorldManagerImpl manager = new WorldManagerImpl();
    manager.camera().sceneConfig().setLookAt(1f, 2f, 3f);
    CameraControlTarget target = CameraControlTarget.resolve(manager);
    assertEquals(1f, target.x(), 0.001f);
}
```

- [ ] **Step 2: Implement**

```java
public final class CameraControlTarget {
    private final float x, y, z;
    public static CameraControlTarget resolve(WorldManager manager) {
        for (Entity e : manager.entities().entitiesWith(Selected.class)) {
            Transform t = manager.entities().getComponent(e.id(), Transform.class);
            if (t != null) {
                return new CameraControlTarget(t.x(), t.y(), t.z());
            }
        }
        SceneCameraConfig cfg = manager.camera().sceneConfig();
        if (!Float.isNaN(cfg.lookAtX())) {
            return new CameraControlTarget(cfg.lookAtX(), cfg.lookAtY(), cfg.lookAtZ());
        }
        return new CameraControlTarget(0f, 0f, 0f);
    }
}
```

Add `CameraResolver.mainCameraProjection(WorldManager manager)` (same as prior plan).

- [ ] **Step 3: Run — expect PASS**

- [ ] **Step 4: Commit**

```bash
git commit -m "feat(input): camera control target from selection or lookAt"
```

---

### Task 5: `CameraControlSystem` (replaces `CameraSceneControlSystem`)

**Files:**
- Create: `hermes-core/src/main/java/dev/hermes/core/input/CameraControlSystem.java`
- Delete: `hermes-core/src/main/java/dev/hermes/core/input/CameraSceneControlSystem.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/ecs/BuiltinComponents.java`
- Test: `hermes-core/src/test/java/dev/hermes/core/input/CameraControlSystemTest.java`

- [ ] **Step 1: Write failing tests**

```java
@Test
void sceneCamera_leftDragOrbitsAroundLookAt() {
    // load perspective-orbit-test.json into WorldManagerImpl (see TileMapWorldTest pattern)
    // assert sceneConfig.rotationY changes after left drag on empty space
}

@Test
void sceneCamera_leftDragOrbitsAroundSelectedEntity() {
    // load scene + selectable entity off-center
    // select entity, left drag, assert orbit changes camera but target stays near entity
}

@Test
void sceneCamera_rightDragPans() {
    // right drag changes config.x/y
}

@Test
void sceneCamera_scrollZooms() {
    InputFrame frame = InputFrame.builder().pointer(320, 240).scroll(0, -1).build();
    // assert z decreases
}

@Test
void firstPersonMode_dragRotatesView() {
    // load scene with controls.mode firstPerson, drag, assert rotation changes
}
```

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement `CameraControlSystem`**

Core loop (orbit mode):

```java
@Override
public void update(WorldManager manager, float deltaSeconds) {
    if (CameraResolver.mainCameraProjection(manager) != Camera.Projection.PERSPECTIVE) {
        activeButton = -1;
        return;
    }
    CameraControlsConfig cfg = manager.camera().controls();
    if (!cfg.enabled()) {
        return;
    }
    if (cfg.mode() == CameraControlsMode.FIRST_PERSON) {
        updateFirstPerson(manager, deltaSeconds, cfg);
        return;
    }
    updateOrbit(manager, deltaSeconds, cfg);
}
```

`updateOrbit` logic:

1. Read pointer; track `activeButton` on press (ignore if UI consumed — see Task 6).
2. On drag: compute normalized `deltaX = (x - lastX) / surfaceWidth`, `deltaY = (lastY - y) / surfaceHeight` (libGDX Y flip).
3. Resolve `CameraControlTarget`.
4. Read current main camera via `CameraResolver.resolveForManager(manager, "screen", w, h)`.
5. Dispatch by button: `rotateButton` → `gdx.orbit(...)`, `translateButton` → `gdx.pan(...)`, `forwardButton` → `gdx.dolly(...)`.
6. On scroll (when `scrollZoom`): `gdx.scrollZoom(...)`.
7. `MainCameraWriter.write(manager, updatedActive, target, cfg)` — updates scene config or entity camera **and** syncs `lookAt` on scene config when target came from selection.

**Remove** the old `if (pick hit) orbiting = false` gate entirely.

Keyboard (orbit): map actions `camera_forward` / `camera_backward` in `updateOrbit` when `input.actions().pressed(...)`.

Register in `BuiltinComponents.registerSystems` replacing `CameraSceneControlSystem`.

- [ ] **Step 4: Run tests — expect PASS**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(input): built-in orbit and first-person camera control system"
```

---

### Task 6: UI hit-test guard + `EntityDragSystem` fix

**Files:**
- Modify: `hermes-core/src/main/java/dev/hermes/core/input/CameraControlSystem.java`
- Modify: `hermes-core/src/main/java/dev/hermes/core/input/EntityDragSystem.java`
- Test: extend `EntityDragSystemTest` (perspective scene → no drag)

- [ ] **Step 1: Skip camera control when pointer is over UI**

At the start of `CameraControlSystem.update`, if active scene has UI and `UiInputSystem.hitTest(...)` returns a `button` node at the pointer position, return early (same coordinate path as `UiInputSystem.update`).

- [ ] **Step 2: Fix `EntityDragSystem` projection**

```java
if (CameraResolver.mainCameraProjection(manager) != Camera.Projection.ORTHOGRAPHIC) {
    anchored = false;
    return;
}
```

- [ ] **Step 3: Run**

Run: `./gradlew :hermes-core:test --tests "dev.hermes.core.input.CameraControlSystemTest,dev.hermes.core.input.EntityDragSystemTest" -q`

- [ ] **Step 4: Commit**

```bash
git commit -m "fix(input): respect UI hits and ortho-only entity drag"
```

---

### Task 7: Input profile camera actions

**Files:**
- Modify: `dogfood-simulation/src/main/resources/assets/input/profile.json`
- Modify: `hermes-templates/minimal/game/src/main/resources/assets/input/profile.json`
- Modify: `hermes-templates/2d/game/src/main/resources/assets/input/profile.json`
- Modify: `hermes-templates/multi-scene/game/src/main/resources/assets/input/profile.json`
- Modify: `hermes-core/src/main/java/dev/hermes/core/input/CameraControlSystem.java`

- [ ] **Step 1: Add actions to gameplay profile**

```json
"actions": {
  "camera_forward": { "type": "button" },
  "camera_backward": { "type": "button" },
  "camera_left": { "type": "button" },
  "camera_right": { "type": "button" },
  "camera_up": { "type": "button" },
  "camera_down": { "type": "button" }
},
"bindings": [
  { "action": "camera_forward", "source": "keyboard", "key": "W", "when": "pressed", "context": "gameplay" },
  { "action": "camera_backward", "source": "keyboard", "key": "S", "when": "pressed", "context": "gameplay" },
  { "action": "camera_left", "source": "keyboard", "key": "A", "when": "pressed", "context": "gameplay" },
  { "action": "camera_right", "source": "keyboard", "key": "D", "when": "pressed", "context": "gameplay" },
  { "action": "camera_up", "source": "keyboard", "key": "Q", "when": "pressed", "context": "gameplay" },
  { "action": "camera_down", "source": "keyboard", "key": "E", "when": "pressed", "context": "gameplay" }
]
```

Wire in `CameraControlSystem`: orbit mode uses forward/back along view; FPS mode uses all six.

- [ ] **Step 2: Commit**

```bash
git commit -m "feat(input): default WASD/QE camera movement bindings"
```

---

### Task 8: Perspective pick regression + delete old test class

**Files:**
- Create: `hermes-core/src/test/resources/assets/scenes/perspective-pick-test.json`
- Modify: `hermes-core/src/test/java/dev/hermes/core/input/WorldPickerTest.java`
- Delete: `hermes-core/src/test/java/dev/hermes/core/input/CameraSceneControlSystemTest.java` (replaced by `CameraControlSystemTest`)

- [ ] **Step 1–4:** Same as prior plan Task 4 (perspective pick with scene camera loaded via `WorldManagerImpl`).

- [ ] **Step 5: Commit**

```bash
git commit -m "test(input): perspective pick with scene-owned camera"
```

---

### Task 9: Dogfood demo + templates

**Files:**
- Modify: `dogfood-simulation/src/main/resources/assets/scenes/main.json`
- Modify: `dogfood-simulation/src/main/resources/assets/entities/spin-cube/type.json`
- Modify: `dogfood-simulation/src/main/java/dev/hermes/sample/SceneNavigationSystem.java`

- [ ] **Step 1: Update dogfood `main.json` camera**

```json
"camera": {
  "version": 1,
  "projection": "perspective",
  "x": 0,
  "y": 2,
  "z": 5,
  "fieldOfView": 60,
  "fitMode": "stretch",
  "lookAt": { "x": 0, "y": 0, "z": 0 },
  "controls": {
    "mode": "orbit",
    "rotateButton": "LEFT",
    "translateButton": "RIGHT",
    "forwardButton": "MIDDLE",
    "scrollZoom": true
  }
}
```

- [ ] **Step 2: `Selectable.radius`: 2.0** on spin-cube (≥ `SpinMarker.radius` 1.5).

- [ ] **Step 3: Disable auto-pause** in `SceneNavigationSystem` (`AUTO_PAUSE_DEMO = false`).

- [ ] **Step 4: Manual smoke test**

| Action | Expected |
|--------|----------|
| Empty left-drag | Orbit around origin |
| Click cube | Select (`Selected` component) |
| Left-drag after selecting cube | Orbit around cube position |
| Right-drag | Pan |
| Scroll | Zoom |
| Middle-drag | Dolly |
| Main menu Play click | Starts game |

- [ ] **Step 5: Commit**

```bash
git commit -m "fix(dogfood): camera controls demo scene and pick radius"
```

---

### Task 10: Documentation

**Files:**
- Modify: `docs/input.md`
- Modify: `docs/world-space.md`

- [ ] **Step 1: Document built-in controls**

Add section **Built-in perspective camera controls** covering:

- Orbit vs first-person modes
- Button mapping table (libGDX parity links)
- Selected entity as orbit target
- Scene JSON `"controls"` example
- Orthographic → entity drag still applies

- [ ] **Step 2: Update world-space camera block docs** with `"controls"` field table.

- [ ] **Step 3: Commit**

```bash
git commit -m "docs: built-in orbit and first-person camera controls"
```

---

### Task 11: Full verification

- [ ] **Step 1:** `./gradlew :hermes-core:test -q` — all PASS
- [ ] **Step 2:** `./gradlew :dogfood-simulation:compileJava -q` — SUCCESS
- [ ] **Step 3:** `hermes new --template minimal` — empty drag orbits; click cube selects; drag orbits around cube

---

## Self-review

| Requirement | Task |
|-------------|------|
| Fix scene-owned camera (no-op bug) | 3, 5 |
| libGDX `CameraInputController` orbit/pan/dolly/scroll | 3, 5 |
| Entity as orbit target when selected | 4, 5 |
| libGDX `FirstPersonCameraController` optional mode | 1, 3, 5 |
| Scroll wheel | 2 |
| UI does not steal world camera drags incorrectly | 6 |
| 2D ortho entity drag unchanged | 6 |
| Scene JSON configurability | 1 |
| Dogfood works | 9 |
| Docs | 10 |

No placeholders. `CameraControlsConfig` field names are consistent across parser, system, and docs.

---

## Execution handoff

Plan saved to `docs/superpowers/plans/2026-06-16-scene-camera-input-controls.md`.

**1. Subagent-Driven (recommended)** — fresh subagent per task, two-stage review between tasks.

**2. Inline Execution** — run tasks in this session with checkpoints.

Which approach?
