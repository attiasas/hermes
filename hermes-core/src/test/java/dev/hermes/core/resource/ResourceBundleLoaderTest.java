package dev.hermes.core.resource;

import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceBundleLoaderTest {

    @Test
    void parsesBundleJson() {
        String json =
                "{\n"
                        + "  \"version\": 1,\n"
                        + "  \"id\": \"main-menu\",\n"
                        + "  \"resources\": [\n"
                        + "    { \"ref\": \"@logo\", \"kind\": \"texture\" },\n"
                        + "    { \"ref\": \"models/cube.obj\", \"kind\": \"model\" },\n"
                        + "    { \"ref\": \"sfx/ui/hover.wav\", \"kind\": \"sound\" }\n"
                        + "  ]\n"
                        + "}\n";
        ResourceBundle bundle = ResourceBundleLoader.parse(json);
        assertEquals("main-menu", bundle.id());
        assertEquals(1, bundle.version());
        assertEquals(3, bundle.resources().size());

        ResourceBundle.Entry logo = bundle.resources().get(0);
        assertEquals(ResourceRef.of("@logo"), logo.ref());
        assertEquals(ResourceKind.TEXTURE, logo.kind());

        ResourceBundle.Entry model = bundle.resources().get(1);
        assertEquals(ResourceRef.of("models/cube.obj"), model.ref());
        assertEquals(ResourceKind.MODEL, model.kind());

        ResourceBundle.Entry sound = bundle.resources().get(2);
        assertEquals(ResourceRef.of("sfx/ui/hover.wav"), sound.ref());
        assertEquals(ResourceKind.SOUND, sound.kind());
    }

    @Test
    void loadsBundleFromClasspath() {
        TestGdx.initClasspathFiles();
        ResourceBundle bundle =
                ResourceBundleLoader.loadById("assets/resources/bundles", "test-bundle");
        assertEquals("test-bundle", bundle.id());
        assertEquals(3, bundle.resources().size());
        assertEquals(ResourceRef.of("@logo"), bundle.resources().get(0).ref());
        assertEquals(ResourceKind.TEXTURE, bundle.resources().get(0).kind());
    }
}
