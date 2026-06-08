package dev.hermes.api.resource;

/** Snapshot of aggregate loading progress for the active loading overlay. */
public final class LoadProgress {
    private final float fraction;
    private final String label;

    public LoadProgress(float fraction, String label) {
        this.fraction = fraction;
        this.label = label;
    }

    public float fraction() {
        return fraction;
    }

    public String label() {
        return label;
    }
}
