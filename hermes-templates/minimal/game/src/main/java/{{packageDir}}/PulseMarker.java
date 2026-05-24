package

{{package}};

import dev.hermes.api.Component;

/**
 * Example custom component: scales the entity in and out.
 */
public final class PulseMarker implements Component {

    private float amplitude = 0.1f;
    private float speed = 2f;
    private float phase;

    public float amplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float speed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float phase() {
        return phase;
    }

    public void setPhase(float phase) {
        this.phase = phase;
    }
}
