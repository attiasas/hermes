package dev.hermes.core.resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Desktop path: decode on a worker pool; upload still runs via {@code Gdx.app.postRunnable}. */
final class ThreadPoolLoadStrategy implements LoadExecutionStrategy {

    private ExecutorService executor;

    @Override
    public void enqueue(AsyncJob job) {
        executor().execute(job::runPhaseA);
    }

    @Override
    public void tick() {
        // Thread pool decodes off-thread; cooperative tick is a no-op.
    }

    @Override
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    private ExecutorService executor() {
        if (executor == null) {
            if (ResourcePlatform.isHtmlPlatform()) {
                throw new IllegalStateException("Thread pool load strategy is unavailable on HTML");
            }
            int threads = Math.min(4, Runtime.getRuntime().availableProcessors());
            executor = Executors.newFixedThreadPool(threads, new ResourceLoadThreadFactory());
        }
        return executor;
    }

    private static final class ResourceLoadThreadFactory implements ThreadFactory {
        private static final AtomicInteger NEXT = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "hermes-resource-load-" + NEXT.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
