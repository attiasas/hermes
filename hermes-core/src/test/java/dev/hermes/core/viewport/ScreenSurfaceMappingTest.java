package dev.hermes.core.viewport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.hermes.api.math.Vec2;
import dev.hermes.core.ecs.WorldImpl;
import org.junit.jupiter.api.Test;

final class ScreenSurfaceMappingTest {

    @Test
    void screenCenterMapsToSurfaceCenter() {
        ViewportServiceImpl viewport = new ViewportServiceImpl();
        viewport.onWindowResize(800, 600);
        WorldImpl world = new WorldImpl();
        Vec2 out = new Vec2();
        viewport.mapScreenToSurface(400, 300, viewport.backbufferSurface(world), out);
        assertEquals(400f, out.x, 0.01f);
        assertEquals(300f, out.y, 0.01f);
    }
}
