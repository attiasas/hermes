package dev.hermes.core.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.hermes.api.scene.SceneChangeRequest;
import org.junit.jupiter.api.Test;

final class SceneChangeRequestTest {

    @Test
    void goToFactory() {
        SceneChangeRequest request = SceneChangeRequest.goTo("main");
        assertEquals(SceneChangeRequest.Kind.GO_TO, request.kind());
        assertEquals("main", request.sceneId());
    }

    @Test
    void pushFactory() {
        SceneChangeRequest request = SceneChangeRequest.push("pause");
        assertEquals(SceneChangeRequest.Kind.PUSH, request.kind());
        assertEquals("pause", request.sceneId());
    }

    @Test
    void popFactory() {
        SceneChangeRequest request = SceneChangeRequest.pop();
        assertEquals(SceneChangeRequest.Kind.POP, request.kind());
        assertNull(request.sceneId());
    }
}
