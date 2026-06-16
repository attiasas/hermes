package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceLoaderRegistration;

/** Test SPI entry registering a {@link ResourceKind#BINARY} loader. */
public final class TestBinaryResourceLoaderRegistration implements ResourceLoaderRegistration {

    @Override
    public void register(dev.hermes.api.resource.ResourceLoaderRegistry registry) {
        registry.register(ResourceKind.BINARY, new TestBinaryResourceLoader());
    }
}
