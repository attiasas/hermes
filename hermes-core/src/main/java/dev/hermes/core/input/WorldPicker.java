package dev.hermes.core.input;

import dev.hermes.api.Entity;
import dev.hermes.api.ecs.Camera;
import dev.hermes.api.ecs.Selectable;
import dev.hermes.api.ecs.Transform;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.WorldManager;
import dev.hermes.api.world.WorldSpace;
import dev.hermes.api.input.PickHit;
import dev.hermes.api.input.PickLayer;
import dev.hermes.api.math.ScreenRay;
import dev.hermes.api.math.Vec2;
import dev.hermes.api.math.Vec3;
import dev.hermes.api.viewport.RenderSurfaceDesc;
import dev.hermes.api.viewport.SceneViewport;
import dev.hermes.api.viewport.ViewportService;
import dev.hermes.core.ecs.ActiveCamera;
import dev.hermes.core.ecs.CameraResolver;
import dev.hermes.core.ecs.WorldManagerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Screen-space picking against {@link Selectable} entities via {@link ViewportService}. */
final class WorldPicker {

    private static final float ORTHO_PLANE_Z = 0f;
    private static final float UNBOUNDED_PICK_RADIUS = 4096f;

    private final ViewportService viewport;

    WorldPicker(ViewportService viewport) {
        this.viewport = viewport;
    }

    Optional<PickHit> pick(EntityStore entities, float screenX, float screenY, PickLayer layer) {
        RenderSurfaceDesc surface = viewport.backbufferSurface(entities);
        Vec2 onSurface = new Vec2();
        viewport.mapScreenToSurface(screenX, screenY, surface, onSurface);
        SceneViewport vp = viewport.forSurface(entities, surface);

        if (isOrthographic(entities, surface)) {
            Vec3 worldPt = new Vec3();
            vp.screenToWorld(onSurface.x, onSurface.y, ORTHO_PLANE_Z, worldPt);
            return pickOrtho(worldPt, entities, layer);
        }
        ScreenRay ray = vp.screenRay(onSurface.x, onSurface.y);
        return pickPerspective(ray, entities, layer);
    }

    private static Iterable<Entity> selectableCandidates(
            EntityStore entities, float worldX, float worldY, float worldZ, float queryRadius) {
        WorldManager manager = WorldManagerRegistry.lookup(entities);
        if (manager == null) {
            return entities.entitiesWith(Selectable.class);
        }
        WorldSpace space = manager.space();
        space.spatial().rebuild(entities);
        List<Entity> near =
                worldZ != 0f || queryRadius > 512f
                        ? space.queryNear(worldX, worldY, worldZ, queryRadius)
                        : space.queryNear(worldX, worldY, queryRadius);
        List<Entity> selectable = new ArrayList<>();
        for (Entity entity : near) {
            if (entities.hasComponent(entity.id(), Selectable.class)) {
                selectable.add(entity);
            }
        }
        if (selectable.isEmpty()) {
            for (Entity entity : entities.entitiesWith(Selectable.class)) {
                selectable.add(entity);
            }
        }
        return selectable;
    }

    private static float pickQueryRadius(EntityStore entities) {
        WorldManager manager = WorldManagerRegistry.lookup(entities);
        float maxSelectable = 0f;
        for (Entity entity : entities.entitiesWith(Selectable.class)) {
            Selectable selectable = entities.getComponent(entity.id(), Selectable.class);
            if (selectable != null) {
                maxSelectable = Math.max(maxSelectable, selectable.radius());
            }
        }
        if (manager == null || manager.space().bounds().isUnbounded()) {
            return Math.max(maxSelectable, UNBOUNDED_PICK_RADIUS);
        }
        var bounds = manager.space().bounds();
        float dx = bounds.maxX() - bounds.minX();
        float dy = bounds.maxY() - bounds.minY();
        float diagonal = (float) Math.hypot(dx, dy);
        return Math.max(maxSelectable, diagonal);
    }

    private static boolean isOrthographic(EntityStore entities, RenderSurfaceDesc surface) {
        ActiveCamera active =
                CameraResolver.resolveForPass(
                        entities, surface.targetId(), surface.pixelWidth(), surface.pixelHeight());
        return active.projection() == Camera.Projection.ORTHOGRAPHIC;
    }

    private static Optional<PickHit> pickOrtho(Vec3 worldPt, EntityStore entities, PickLayer filter) {
        PickHit best = null;
        float bestDist = Float.MAX_VALUE;
        float queryRadius = pickQueryRadius(entities);

        for (Entity entity : selectableCandidates(entities, worldPt.x, worldPt.y, 0f, queryRadius)) {
            Selectable selectable = entities.getComponent(entity.id(), Selectable.class);
            if (selectable == null || !selectable.enabled() || !matchesLayer(selectable, filter)) {
                continue;
            }
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform == null) {
                continue;
            }
            float dx = transform.x() - worldPt.x;
            float dy = transform.y() - worldPt.y;
            float dist = (float) Math.hypot(dx, dy);
            if (dist <= selectable.radius() && dist < bestDist) {
                bestDist = dist;
                best =
                        new PickHit(
                                entity.id(),
                                entity.name(),
                                transform.x(),
                                transform.y(),
                                transform.z(),
                                dist);
            }
        }
        return Optional.ofNullable(best);
    }

    private static Optional<PickHit> pickPerspective(ScreenRay ray, EntityStore entities, PickLayer filter) {
        PickHit best = null;
        float bestT = Float.MAX_VALUE;
        float queryRadius = pickQueryRadius(entities);
        float originX = ray.originX;
        float originY = ray.originY;
        float originZ = ray.originZ;

        for (Entity entity :
                selectableCandidates(entities, originX, originY, originZ, queryRadius)) {
            Selectable selectable = entities.getComponent(entity.id(), Selectable.class);
            if (selectable == null || !selectable.enabled() || !matchesLayer(selectable, filter)) {
                continue;
            }
            Transform transform = entities.getComponent(entity.id(), Transform.class);
            if (transform == null) {
                continue;
            }
            Optional<Float> t =
                    raySphereHit(
                            ray,
                            transform.x(),
                            transform.y(),
                            transform.z(),
                            selectable.radius());
            if (t.isPresent() && t.get() < bestT) {
                bestT = t.get();
                float hitX = ray.originX + ray.directionX * bestT;
                float hitY = ray.originY + ray.directionY * bestT;
                float hitZ = ray.originZ + ray.directionZ * bestT;
                best =
                        new PickHit(
                                entity.id(),
                                entity.name(),
                                hitX,
                                hitY,
                                hitZ,
                                bestT);
            }
        }
        return Optional.ofNullable(best);
    }

    private static boolean matchesLayer(Selectable selectable, PickLayer filter) {
        return selectable.layer() == filter;
    }

    /**
     * Ray-sphere intersection; returns smallest positive {@code t} along the ray, or empty if none.
     */
    static Optional<Float> raySphereHit(
            ScreenRay ray, float centerX, float centerY, float centerZ, float radius) {
        float ox = ray.originX - centerX;
        float oy = ray.originY - centerY;
        float oz = ray.originZ - centerZ;
        float dx = ray.directionX;
        float dy = ray.directionY;
        float dz = ray.directionZ;
        float b = 2f * (ox * dx + oy * dy + oz * dz);
        float c = ox * ox + oy * oy + oz * oz - radius * radius;
        float disc = b * b - 4f * c;
        if (disc < 0f) {
            return Optional.empty();
        }
        float sqrtDisc = (float) Math.sqrt(disc);
        float t0 = (-b - sqrtDisc) * 0.5f;
        float t1 = (-b + sqrtDisc) * 0.5f;
        if (t0 > 0f) {
            return Optional.of(t0);
        }
        if (t1 > 0f) {
            return Optional.of(t1);
        }
        return Optional.empty();
    }
}
