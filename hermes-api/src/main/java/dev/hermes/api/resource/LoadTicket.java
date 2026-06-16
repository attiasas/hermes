package dev.hermes.api.resource;

import java.util.Optional;

/** Handle for an async resource or bundle load batch. */
public interface LoadTicket {

    boolean done();

    boolean failed();

    Optional<Throwable> error();

    /** Aggregate progress in the range {@code 0.0 .. 1.0}. */
    float progress();

    /**
     * Blocks until done or failed.
     * <p><b>Desktop:</b> safe from test threads and background loaders.
     * <p><b>HTML:</b> do not call from the main/game loop — freezes the tab.
     * Tests must use a frame pump helper instead.
     */
    void awaitCompletion();
}
