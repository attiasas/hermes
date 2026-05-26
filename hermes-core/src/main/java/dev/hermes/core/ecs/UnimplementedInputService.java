package dev.hermes.core.ecs;

import dev.hermes.api.ecs.World;
import dev.hermes.api.input.GamepadSnapshot;
import dev.hermes.api.input.InputActions;
import dev.hermes.api.input.InputDevices;
import dev.hermes.api.input.InputService;
import dev.hermes.api.input.KeyboardSnapshot;
import dev.hermes.api.input.PickHit;
import dev.hermes.api.input.PickLayer;
import dev.hermes.api.input.PointerSnapshot;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.api.viewport.ViewportService;

import java.util.Optional;

/**
 * Placeholder until {@code InputServiceImpl} lands (input plan Task 4). Keeps {@link HermesEngineImpl} compilable.
 */
final class UnimplementedInputService implements InputService {

    private static final InputActions NO_ACTIONS =
            new InputActions() {
                @Override
                public boolean pressed(String action) {
                    return false;
                }

                @Override
                public boolean justPressed(String action) {
                    return false;
                }

                @Override
                public boolean justReleased(String action) {
                    return false;
                }

                @Override
                public float axis(String action) {
                    return 0f;
                }

                @Override
                public void axis2(String action, float[] out) {
                    if (out != null && out.length >= 2) {
                        out[0] = 0f;
                        out[1] = 0f;
                    }
                }

                @Override
                public String context() {
                    return "";
                }
            };

    private static final KeyboardSnapshot NO_KEYBOARD =
            new KeyboardSnapshot() {
                @Override
                public boolean pressed(int key) {
                    return false;
                }

                @Override
                public boolean justPressed(int key) {
                    return false;
                }

                @Override
                public boolean justReleased(int key) {
                    return false;
                }
            };

    private static final PointerSnapshot NO_POINTER =
            new PointerSnapshot() {
                @Override
                public float screenX() {
                    return 0f;
                }

                @Override
                public float screenY() {
                    return 0f;
                }

                @Override
                public boolean pressed() {
                    return false;
                }

                @Override
                public boolean justPressed(int button) {
                    return false;
                }

                @Override
                public boolean pressed(int button) {
                    return false;
                }
            };

    private static final GamepadSnapshot NO_GAMEPAD =
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

    private static final InputDevices NO_DEVICES =
            new InputDevices() {
                @Override
                public KeyboardSnapshot keyboard() {
                    return NO_KEYBOARD;
                }

                @Override
                public PointerSnapshot pointer() {
                    return NO_POINTER;
                }

                @Override
                public int gamepadCount() {
                    return 0;
                }

                @Override
                public GamepadSnapshot gamepad(int index) {
                    return NO_GAMEPAD;
                }
            };

    private final ViewportService viewport;

    UnimplementedInputService(ViewportService viewport) {
        this.viewport = viewport;
    }

    @Override
    public InputActions actions() {
        return NO_ACTIONS;
    }

    @Override
    public InputDevices devices() {
        return NO_DEVICES;
    }

    @Override
    public SceneViewport viewport(World world) {
        return viewport.forWorld(world);
    }

    @Override
    public Optional<PickHit> pick(World world, float screenX, float screenY) {
        return Optional.empty();
    }

    @Override
    public Optional<PickHit> pick(World world, float screenX, float screenY, PickLayer layer) {
        return Optional.empty();
    }
}
