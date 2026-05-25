package dev.hermes.api.ecs;

/** How a camera viewport rect fits inside its render surface. */
public enum ViewportFitMode {
    /** Fill surface; may distort if aspect differs from design. */
    STRETCH,
    /** Letterbox/pillarbox; preserve {@code designAspect} or surface aspect. */
    LETTERBOX,
    /** Fill surface; crop excess via projection. */
    CROP,
    /** Use viewportWidth/Height literally; no auto aspect. */
    FIXED
}
