package dev.hermes.core.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.math.Vec2;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NormalizedCoordinateTest {

    @BeforeAll
    static void initGdx() {
        TestGdx.initHeadlessGl();
    }

    @Test
    void normalizedCenterMapsToViewportCenter() {
        SceneViewportImpl vp = SceneViewportImpl.forRect(0, 0, 800, 600, 800, 600);
        Vec2 out = new Vec2();
        vp.normalizedToSurface(0.5f, 0.5f, out);
        assertEquals(400f, out.x, 0.01f);
        assertEquals(300f, out.y, 0.01f);
    }
}
