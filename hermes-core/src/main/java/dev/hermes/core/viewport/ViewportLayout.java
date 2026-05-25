package dev.hermes.core.viewport;

/**
 * Computes the drawable viewport rectangle inside a render surface for a given fit policy.
 */
public final class ViewportLayout {

    private ViewportLayout() {
    }

    /** Full-surface rect for stretch mode at the given dimensions. */
    public static Rect4 stretch(int surfaceW, int surfaceH) {
        Rect4 rect = new Rect4();
        compute(surfaceW, surfaceH, 0f, ViewportFitMode.STRETCH, rect);
        return rect;
    }

    public static void compute(
            int surfaceW, int surfaceH, float designAspect, ViewportFitMode mode, Rect4 out) {
        if (mode == ViewportFitMode.STRETCH || mode == ViewportFitMode.FIXED) {
            out.set(0f, 0f, surfaceW, surfaceH);
            return;
        }
        float surfaceAspect = (float) surfaceW / surfaceH;
        float targetAspect = designAspect > 0f ? designAspect : surfaceAspect;
        if (mode == ViewportFitMode.LETTERBOX) {
            computeLetterbox(surfaceW, surfaceH, targetAspect, out);
        } else if (mode == ViewportFitMode.CROP) {
            computeCrop(surfaceW, surfaceH, targetAspect, out);
        } else {
            out.set(0f, 0f, surfaceW, surfaceH);
        }
    }

    private static void computeLetterbox(int surfaceW, int surfaceH, float targetAspect, Rect4 out) {
        float surfaceAspect = (float) surfaceW / surfaceH;
        if (surfaceAspect > targetAspect) {
            float height = surfaceH;
            float width = height * targetAspect;
            out.set((surfaceW - width) * 0.5f, 0f, width, height);
        } else {
            float width = surfaceW;
            float height = width / targetAspect;
            out.set(0f, (surfaceH - height) * 0.5f, width, height);
        }
    }

    private static void computeCrop(int surfaceW, int surfaceH, float targetAspect, Rect4 out) {
        float surfaceAspect = (float) surfaceW / surfaceH;
        if (surfaceAspect > targetAspect) {
            float width = surfaceW;
            float height = width / targetAspect;
            out.set(0f, (surfaceH - height) * 0.5f, width, height);
        } else {
            float height = surfaceH;
            float width = height * targetAspect;
            out.set((surfaceW - width) * 0.5f, 0f, width, height);
        }
    }
}
