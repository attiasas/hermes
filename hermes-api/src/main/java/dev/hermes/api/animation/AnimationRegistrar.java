package dev.hermes.api.animation;

/** Registration sink used by {@link AnimationRegistration} SPI providers. */
public interface AnimationRegistrar {

    void trackResolver(AnimationTrackResolver resolver);

    /**
     * Registers a backend instance.
     *
     * <p>Expected runtime type in hermes-core is {@code dev.hermes.core.animation.AnimationBackend}.
     */
    void backend(Object backend);
}
