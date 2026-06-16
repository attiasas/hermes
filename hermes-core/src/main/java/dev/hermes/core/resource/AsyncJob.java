package dev.hermes.core.resource;

import com.badlogic.gdx.Gdx;

/** Single async resource load: Phase A decode, Phase B upload on the render thread. */
final class AsyncJob {

    private final ResourceKey key;
    private final String path;
    private final ResourceLoader loader;
    private final ResourceCache cache;
    private final LoadTicketImpl ticket;

    AsyncJob(
            ResourceKey key,
            String path,
            ResourceLoader loader,
            ResourceCache cache,
            LoadTicketImpl ticket) {
        this.key = key;
        this.path = path;
        this.loader = loader;
        this.cache = cache;
        this.ticket = ticket;
    }

    void runPhaseA() {
        if (ticket.isTerminal()) {
            return;
        }
        try {
            DecodedPayload decoded = loader.decode(path);
            Gdx.app.postRunnable(() -> runPhaseB(decoded));
        } catch (Throwable error) {
            ticket.fail(error);
        }
    }

    private void runPhaseB(DecodedPayload decoded) {
        if (ticket.isTerminal()) {
            return;
        }
        try {
            if (!cache.contains(key)) {
                Object resource = loader.upload(decoded);
                cache.put(key, resource, () -> loader.dispose(resource));
            }
            ticket.itemComplete();
        } catch (Throwable error) {
            ticket.fail(error);
        }
    }
}
