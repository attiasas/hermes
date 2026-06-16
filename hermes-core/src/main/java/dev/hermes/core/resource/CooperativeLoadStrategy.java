package dev.hermes.core.resource;

import java.util.ArrayDeque;
import java.util.Deque;

/** HTML / test path: decode at most {@code assetsPerFrame} jobs per {@link #tick()}. */
final class CooperativeLoadStrategy implements LoadExecutionStrategy {

    private final Deque<AsyncJob> queue = new ArrayDeque<>();
    private final int assetsPerFrame;

    CooperativeLoadStrategy(int assetsPerFrame) {
        if (assetsPerFrame < 1) {
            throw new IllegalArgumentException("assetsPerFrame must be >= 1");
        }
        this.assetsPerFrame = assetsPerFrame;
    }

    @Override
    public void enqueue(AsyncJob job) {
        queue.addLast(job);
    }

    @Override
    public void tick() {
        int budget = assetsPerFrame;
        while (budget-- > 0 && !queue.isEmpty()) {
            queue.pollFirst().runPhaseA();
        }
    }

    @Override
    public void shutdown() {
        queue.clear();
    }
}
