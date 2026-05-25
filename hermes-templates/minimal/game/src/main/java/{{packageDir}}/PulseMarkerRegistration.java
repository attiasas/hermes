package

{{package}};

import dev.hermes.api.ecs.ComponentRegistration;
import dev.hermes.api.ecs.HermesEngine;

/**
 * Registers {@link PulseMarker} for JSON scenes (ServiceLoader).
 */
public final class PulseMarkerRegistration implements ComponentRegistration {

    @Override
    public void register(HermesEngine engine) {
        engine
                .registry()
                .register(
                        "PulseMarker",
                        PulseMarker.class,
                        data -> {
                            PulseMarker pulse = new PulseMarker();
                            pulse.setAmplitude(data.getFloat("amplitude", 0.1f));
                            pulse.setSpeed(data.getFloat("speed", 2f));
                            return pulse;
                        });
        engine.addSystem(new PulseMarkerSystem());
    }
}
