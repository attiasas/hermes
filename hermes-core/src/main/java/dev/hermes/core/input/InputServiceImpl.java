package dev.hermes.core.input;

import dev.hermes.api.ecs.World;
import dev.hermes.api.input.GamepadSnapshot;
import dev.hermes.api.input.InputActions;
import dev.hermes.api.input.InputButton;
import dev.hermes.api.input.InputDevices;
import dev.hermes.api.input.InputService;
import dev.hermes.api.input.KeyboardSnapshot;
import dev.hermes.api.input.PickHit;
import dev.hermes.api.input.PickLayer;
import dev.hermes.api.input.PointerSnapshot;
import dev.hermes.api.scene.SceneHandle;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.core.HermesLauncherSupport;
import dev.hermes.core.ecs.HermesEngineImpl;

import java.util.Optional;

/** Per-frame input polling, action mapping, and viewport/pick delegation. */
public final class InputServiceImpl implements InputService {

    private final HermesEngineImpl engine;
    private final InputProfile profile;
    private final ActionMapper mapper;
    private final GdxInputReaders readers;
    private final WorldPicker worldPicker;
    private final InputActionsState actionsState = new InputActionsState();
    private InputFrame currentFrame = InputFrame.builder().build();
    private int connectedGamepadCount;

    public InputServiceImpl(HermesEngineImpl engine) {
        this(engine, InputProfileLoader.load(HermesLauncherSupport.inputProfilePath()));
    }

    InputServiceImpl(HermesEngineImpl engine, InputProfile profile) {
        this.engine = engine;
        this.profile = profile;
        this.mapper = new ActionMapper(profile);
        this.readers = new GdxInputReaders(profile);
        this.worldPicker = new WorldPicker(engine.viewport());
    }

    @Override
    public void poll(float deltaSeconds) {
        pollFrame(readers.poll());
    }

    void pollFrame(InputFrame frame) {
        currentFrame = frame;
        connectedGamepadCount = frame.connectedGamepadCount();
        mapper.apply(frame, resolveContext(), actionsState);
    }

    @Override
    public InputActions actions() {
        return actionsState;
    }

    @Override
    public InputDevices devices() {
        return devicesView;
    }

    @Override
    public SceneViewport viewport(World world) {
        return engine.viewport().forWorld(world);
    }

    @Override
    public Optional<PickHit> pick(World world, float screenX, float screenY) {
        return pick(world, screenX, screenY, PickLayer.WORLD);
    }

    @Override
    public Optional<PickHit> pick(World world, float screenX, float screenY, PickLayer layer) {
        return worldPicker.pick(world, screenX, screenY, layer);
    }

    private String resolveContext() {
        SceneHandle active = engine.scenes().active();
        if (active != null) {
            return active.inputContext().orElse(profile.defaultContext());
        }
        return profile.defaultContext();
    }

    private final InputDevices devicesView =
            new InputDevices() {
                @Override
                public KeyboardSnapshot keyboard() {
                    return keyboardView;
                }

                @Override
                public PointerSnapshot pointer() {
                    return pointerView;
                }

                @Override
                public int gamepadCount() {
                    return connectedGamepadCount;
                }

                @Override
                public GamepadSnapshot gamepad(int index) {
                    return index == 0 ? gamepadView : emptyGamepad;
                }
            };

    private final KeyboardSnapshot keyboardView =
            new KeyboardSnapshot() {
                @Override
                public boolean pressed(int key) {
                    return currentFrame.keyboardPressed(key);
                }

                @Override
                public boolean justPressed(int key) {
                    return currentFrame.keyboardJustPressed(key);
                }

                @Override
                public boolean justReleased(int key) {
                    return currentFrame.keyboardJustReleased(key);
                }
            };

    private final PointerSnapshot pointerView =
            new PointerSnapshot() {
                @Override
                public float screenX() {
                    return currentFrame.pointerX();
                }

                @Override
                public float screenY() {
                    return currentFrame.pointerY();
                }

                @Override
                public boolean pressed() {
                    return currentFrame.pointerPressed(InputButton.LEFT)
                            || currentFrame.pointerPressed(InputButton.RIGHT)
                            || currentFrame.pointerPressed(InputButton.MIDDLE);
                }

                @Override
                public boolean justPressed(int button) {
                    return currentFrame.pointerJustPressed(button);
                }

                @Override
                public boolean pressed(int button) {
                    return currentFrame.pointerPressed(button);
                }
            };

    private final GamepadSnapshot gamepadView =
            new GamepadSnapshot() {
                @Override
                public boolean pressed(int button) {
                    return currentFrame.gamepadPressed(button);
                }

                @Override
                public boolean justPressed(int button) {
                    return currentFrame.gamepadJustPressed(button);
                }

                @Override
                public boolean justReleased(int button) {
                    return currentFrame.gamepadJustReleased(button);
                }

                @Override
                public float axis(int axis) {
                    float raw = currentFrame.gamepadAxis(axis);
                    float deadzone = profile.gamepadDeadzone();
                    if (Math.abs(raw) < deadzone) {
                        return 0f;
                    }
                    return raw;
                }
            };

    private static final GamepadSnapshot emptyGamepad =
            new GamepadSnapshot() {
                @Override
                public boolean pressed(int button) {
                    return false;
                }

                @Override
                public boolean justPressed(int button) {
                    return false;
                }

                @Override
                public boolean justReleased(int button) {
                    return false;
                }

                @Override
                public float axis(int axis) {
                    return 0f;
                }
            };

}
