package dev.hermes.core.ui;

/** Dispatches UI widget action strings into the input profile namespace. */
@FunctionalInterface
public interface UiActionPulse {

    void pulseAction(String action);
}
