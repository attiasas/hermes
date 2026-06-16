package dev.hermes.core.resource;

import dev.hermes.api.resource.LoadProgress;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** Routes async decode work through a platform-aware {@link LoadExecutionStrategy}. */
final class AsyncLoadExecutor {

    private final ResourceCache cache;
    private final ResourceLoaderRegistry registry;
    private final ResourcePathResolver resolver;
    private final Supplier<LoadExecutionStrategy> strategySupplier;

    private LoadExecutionStrategy strategy;
    private LoadTicketImpl activeTicket;

    AsyncLoadExecutor(
            ResourceCache cache,
            ResourceLoaderRegistry registry,
            ResourcePathResolver resolver,
            Supplier<LoadExecutionStrategy> strategySupplier) {
        this.cache = Objects.requireNonNull(cache, "cache");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        this.strategySupplier = Objects.requireNonNull(strategySupplier, "strategySupplier");
        this.strategy = strategySupplier.get();
    }

    void resetStrategy() {
        strategy.shutdown();
        strategy = strategySupplier.get();
    }

    LoadTicketImpl load(ResourceRef ref, ResourceKind kind) {
        ResourcePathResolver.Resolved resolved = resolver.resolve(ref, kind);
        ResourceKey key = new ResourceKey(ref, resolved.kind());
        LoadTicketImpl ticket = beginTicket(ref.raw(), 1);
        enqueueOrComplete(key, resolved.path(), resolved.kind(), ticket);
        return ticket;
    }

    LoadTicketImpl loadBundle(String bundleId, List<ResourceBundle.Entry> entries) {
        LoadTicketImpl ticket = beginTicket(bundleId, entries.size());
        for (ResourceBundle.Entry entry : entries) {
            ResourcePathResolver.Resolved resolved = resolver.resolve(entry.ref(), entry.kind());
            ResourceKey key = new ResourceKey(entry.ref(), resolved.kind());
            enqueueOrComplete(key, resolved.path(), resolved.kind(), ticket);
        }
        return ticket;
    }

    void tick() {
        strategy.tick();
    }

    void shutdown() {
        strategy.shutdown();
        activeTicket = null;
    }

    Optional<LoadProgress> activeProgress() {
        LoadTicketImpl ticket = activeTicket;
        if (ticket == null || ticket.done() || ticket.failed()) {
            return Optional.empty();
        }
        return Optional.of(new LoadProgress(ticket.progress(), ticket.label()));
    }

    private LoadTicketImpl beginTicket(String label, int total) {
        LoadTicketImpl ticket = new LoadTicketImpl(label, total);
        activeTicket = ticket;
        return ticket;
    }

    private void enqueueOrComplete(
            ResourceKey key, String path, ResourceKind kind, LoadTicketImpl ticket) {
        if (ticket.isTerminal()) {
            return;
        }
        if (cache.contains(key)) {
            ticket.itemComplete();
            return;
        }
        ResourceLoader loader = registry.require(kind);
        strategy.enqueue(new AsyncJob(key, path, loader, cache, ticket));
    }
}
