package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceCatalogLoaderTest {

    @Test
    void parsesEntries() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"entries\": {\n"
                        + "    \"@logo\": { \"path\": \"textures/logo.png\", \"kind\": \"texture\" }\n"
                        + "  }\n"
                        + "}\n";
        ResourceCatalog catalog = ResourceCatalogLoader.parse(json);
        ResourceCatalog.Entry entry = catalog.resolve(ResourceRef.of("@logo"));
        assertEquals("textures/logo.png", entry.path());
        assertEquals(ResourceKind.TEXTURE, entry.kind());
    }
}
