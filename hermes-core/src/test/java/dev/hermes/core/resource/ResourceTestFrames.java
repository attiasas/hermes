package dev.hermes.core.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import dev.hermes.api.resource.LoadTicket;

import static org.junit.jupiter.api.Assertions.fail;

/** Test helper to advance cooperative async loads without blocking the main loop. */
public final class ResourceTestFrames {

    private ResourceTestFrames() {}

    public static void pumpUntilDone(ResourceManagerImpl manager, LoadTicket ticket, int maxFrames) {
        for (int frame = 0; frame < maxFrames && !ticket.done() && !ticket.failed(); frame++) {
            manager.tick();
            flushPostedRunnables();
        }
        assertTerminal(ticket, "Async load did not complete within " + maxFrames + " frames");
    }

    /** Desktop path: block on {@link LoadTicket#awaitCompletion()} while flushing Phase B runnables. */
    public static void awaitWithFlush(LoadTicket ticket) {
        Thread waiter = new Thread(ticket::awaitCompletion, "resource-load-await");
        waiter.setDaemon(true);
        waiter.start();
        try {
            for (int attempt = 0; attempt < 600 && !ticket.done() && !ticket.failed(); attempt++) {
                flushPostedRunnables();
                Thread.sleep(10);
            }
            waiter.join(5000);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for async load", interrupted);
        }
        assertTerminal(ticket, "Async load did not complete");
    }

    private static void assertTerminal(LoadTicket ticket, String timeoutMessage) {
        if (ticket.failed()) {
            ticket.error().ifPresent(error -> fail("Async load failed", error));
        }
        if (!ticket.done()) {
            fail(timeoutMessage);
        }
    }

    static void flushPostedRunnables() {
        if (Gdx.app instanceof HeadlessApplication) {
            ((HeadlessApplication) Gdx.app).executeRunnables();
        }
    }
}
