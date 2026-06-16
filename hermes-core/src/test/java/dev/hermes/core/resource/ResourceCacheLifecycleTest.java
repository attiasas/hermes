package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceCacheLifecycleTest {

    @Test
    void disposesWhenRefCountHitsZero() {
        ResourceCache cache = new ResourceCache();
        AtomicBoolean disposed = new AtomicBoolean(false);
        Object payload = new Object();
        ResourceKey key = new ResourceKey(ResourceRef.of("a.png"), ResourceKind.TEXTURE);

        cache.put(key, payload, () -> disposed.set(true));
        cache.retain(key);
        cache.release(key);
        assertFalse(disposed.get());
        cache.release(key);
        assertTrue(disposed.get());
        assertFalse(cache.contains(key));
    }

    @Test
    void releaseGroupDecrementsAllInGroup() {
        ResourceCache cache = new ResourceCache();
        ResourceKey key1 = new ResourceKey(ResourceRef.of("a.png"), ResourceKind.TEXTURE);
        ResourceKey key2 = new ResourceKey(ResourceRef.of("b.png"), ResourceKind.TEXTURE);
        AtomicBoolean disposed1 = new AtomicBoolean(false);
        AtomicBoolean disposed2 = new AtomicBoolean(false);

        cache.put(key1, new Object(), () -> disposed1.set(true));
        cache.put(key2, new Object(), () -> disposed2.set(true));

        cache.retainGroup("scene:main", key1);
        cache.retainGroup("scene:main", key2);

        assertTrue(cache.contains(key1));
        assertTrue(cache.contains(key2));

        cache.releaseGroup("scene:main");

        assertFalse(disposed1.get());
        assertFalse(disposed2.get());

        cache.release(key1);
        cache.release(key2);

        assertTrue(disposed1.get());
        assertTrue(disposed2.get());
        assertFalse(cache.contains(key1));
        assertFalse(cache.contains(key2));
    }

    @Test
    void getReturnsPayloadWhileCached() {
        ResourceCache cache = new ResourceCache();
        Object payload = new Object();
        ResourceKey key = new ResourceKey(ResourceRef.of("a.png"), ResourceKind.TEXTURE);

        cache.put(key, payload, () -> {});

        assertSame(payload, cache.get(key));
        cache.release(key);
        assertFalse(cache.contains(key));
    }

    @Test
    void disposeClearsAllEntries() {
        ResourceCache cache = new ResourceCache();
        AtomicBoolean disposed1 = new AtomicBoolean(false);
        AtomicBoolean disposed2 = new AtomicBoolean(false);
        ResourceKey key1 = new ResourceKey(ResourceRef.of("a.png"), ResourceKind.TEXTURE);
        ResourceKey key2 = new ResourceKey(ResourceRef.of("b.png"), ResourceKind.TEXTURE);

        cache.put(key1, new Object(), () -> disposed1.set(true));
        cache.put(key2, new Object(), () -> disposed2.set(true));

        cache.dispose();

        assertTrue(disposed1.get());
        assertTrue(disposed2.get());
        assertFalse(cache.contains(key1));
        assertFalse(cache.contains(key2));
    }
}
