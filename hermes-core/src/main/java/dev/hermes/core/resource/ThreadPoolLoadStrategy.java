package dev.hermes.core.resource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desktop path: decode on worker threads; upload still runs via {@code Gdx.app.postRunnable}.
 *
 * <p>Uses {@link Thread} rather than {@code java.util.concurrent} so TeaVM can compile this class
 * even though HTML always selects {@link CooperativeLoadStrategy} at runtime.
 */
final class ThreadPoolLoadStrategy implements LoadExecutionStrategy {

    private static final AtomicInteger NEXT = new AtomicInteger();

    @Override
    public void enqueue(AsyncJob job) {
        if (ResourcePlatform.isHtmlPlatform()) {
            throw new IllegalStateException("Thread pool load strategy is unavailable on HTML");
        }
        Thread thread =
                new Thread(job::runPhaseA, "hermes-resource-load-" + NEXT.incrementAndGet());
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void tick() {
        // Worker threads decode off-thread; cooperative tick is a no-op.
    }

    @Override
    public void shutdown() {
        // Fire-and-forget daemon workers; nothing to join or shut down.
    }
}
