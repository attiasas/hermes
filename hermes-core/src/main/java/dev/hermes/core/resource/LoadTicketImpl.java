package dev.hermes.core.resource;

import dev.hermes.api.resource.LoadTicket;

import java.util.Optional;

/** Mutable load ticket tracking batch progress for async loads. */
final class LoadTicketImpl implements LoadTicket {

    private final int total;
    private final String label;
    private final Object lock = new Object();

    private int completed;
    private boolean done;
    private boolean failed;
    private Throwable error;

    LoadTicketImpl(String label, int total) {
        this.label = label;
        this.total = total;
        if (total <= 0) {
            done = true;
        }
    }

    String label() {
        return label;
    }

    boolean isTerminal() {
        synchronized (lock) {
            return done || failed;
        }
    }

    void itemComplete() {
        synchronized (lock) {
            if (failed || done) {
                return;
            }
            completed++;
            if (completed >= total) {
                done = true;
                lock.notifyAll();
            }
        }
    }

    void fail(Throwable cause) {
        synchronized (lock) {
            if (failed || done) {
                return;
            }
            failed = true;
            error = cause;
            lock.notifyAll();
        }
    }

    @Override
    public boolean done() {
        synchronized (lock) {
            return done;
        }
    }

    @Override
    public boolean failed() {
        synchronized (lock) {
            return failed;
        }
    }

    @Override
    public Optional<Throwable> error() {
        synchronized (lock) {
            return Optional.ofNullable(error);
        }
    }

    @Override
    public float progress() {
        synchronized (lock) {
            if (total <= 0) {
                return 1f;
            }
            return (float) completed / total;
        }
    }

    @Override
    public void awaitCompletion() {
        synchronized (lock) {
            while (!done && !failed) {
                try {
                    lock.wait();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
