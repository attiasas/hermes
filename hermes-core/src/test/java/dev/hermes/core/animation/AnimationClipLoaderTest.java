package dev.hermes.core.animation;

import dev.hermes.api.animation.AnimationClip;
import dev.hermes.api.animation.AnimationTrack;
import dev.hermes.api.animation.Interpolation;
import dev.hermes.api.animation.Keyframe;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.ResourceAccess;
import dev.hermes.core.resource.ResourceManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnimationClipLoaderTest {

    private ResourceManagerImpl resources;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        resources = ResourceManagerImpl.createDefault();
    }

    @Test
    void loadsAnimationClipViaResourceService() {
        ResourceRef clipRef = ResourceRef.of("animations/test-clip.json");

        resources.loadSync(clipRef, ResourceKind.ANIMATION_CLIP);
        assertTrue(resources.isLoaded(clipRef, ResourceKind.ANIMATION_CLIP));

        AnimationClip clip = ResourceAccess.animationClip(resources, clipRef);
        assertEquals(1, clip.version());
        assertEquals(0.8f, clip.duration());
        assertTrue(clip.loop());
        assertEquals(3, clip.tracks().size());

        AnimationTrack scaleX = clip.tracks().get(0);
        assertEquals("Transform.scaleX", scaleX.target());
        assertEquals(Interpolation.LINEAR, scaleX.interpolation());
        assertEquals(2, scaleX.keyframes().size());
        Keyframe scaleMid = scaleX.keyframes().get(1);
        assertEquals(0.5f, scaleMid.t());
        assertEquals(1.15f, scaleMid.v());

        AnimationTrack bodyFrame = clip.tracks().get(1);
        assertEquals("parts.body.frame", bodyFrame.target());
        assertEquals(Interpolation.STEP, bodyFrame.interpolation());
        Keyframe bodyKf = bodyFrame.keyframes().get(1);
        assertEquals(0.2f, bodyKf.t());
        assertEquals(1f, bodyKf.v());

        AnimationTrack uniforms = clip.tracks().get(2);
        assertEquals("Material.uniforms.tint", uniforms.target());
        Keyframe tint = uniforms.keyframes().get(0);
        assertFalse(tint.hasScalarValue());
        assertArrayEquals(new float[]{1f, 0.75f, 0.25f, 1f}, tint.vArray());
    }
}
