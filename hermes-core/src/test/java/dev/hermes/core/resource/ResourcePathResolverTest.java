package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourcePathResolverTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initClasspathFiles();
    }

    @Test
    void resolvesAliasViaCatalog() {
        ResourceCatalog catalog =
                ResourceCatalogLoader.parse(
                        "{\"version\":1,\"entries\":{\"@logo\":{\"path\":\"textures/logo.png\",\"kind\":\"texture\"}}}");
        ResourcePathResolver resolver = new ResourcePathResolver(catalog);
        ResourcePathResolver.Resolved resolved = resolver.resolve(ResourceRef.of("@logo"));
        assertEquals("textures/logo.png", resolved.path());
        assertEquals(ResourceKind.TEXTURE, resolved.kind());
    }

    @Test
    void plainPathUsesInferredKindWhenUnknown() {
        ResourcePathResolver resolver = new ResourcePathResolver(ResourceCatalog.empty());
        ResourcePathResolver.Resolved resolved =
                resolver.resolve(ResourceRef.of("models/cube.obj"), ResourceKind.MODEL);
        assertEquals("models/cube.obj", resolved.path());
        assertEquals(ResourceKind.MODEL, resolved.kind());
    }
}
