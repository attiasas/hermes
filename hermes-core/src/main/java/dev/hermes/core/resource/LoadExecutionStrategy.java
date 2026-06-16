package dev.hermes.core.resource;

/** Platform-specific execution of async load Phase A (decode). */
interface LoadExecutionStrategy {

    void enqueue(AsyncJob job);

    /** Advances cooperative queue; no-op for thread-pool strategy. */
    void tick();

    void shutdown();
}
