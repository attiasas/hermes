package dev.hermes.core.resource;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.hermes.api.ecs.SpriteSheet;
import dev.hermes.api.resource.ResourceKind;
import dev.hermes.api.resource.ResourceRef;
import dev.hermes.core.TestGdx;
import dev.hermes.core.resource.loaders.SpriteSheetResourceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpriteSheetResourceLoaderTest {

    private ResourceManagerImpl resources;

    @BeforeEach
    void setUp() {
        TestGdx.initClasspathFiles();
        TestGdx.initHeadlessGl();
        resources = ResourceManagerImpl.createDefault();
    }

    @Test
    void sheet4x1_frame2_returnsCorrectRegionOffset() {
        String texturePath = "textures/test-sheet-4x1.png";
        SpriteSheet sheet = new SpriteSheet();
        sheet.setColumns(4);
        sheet.setRows(1);
        sheet.setFrameWidth(1);
        sheet.setFrameHeight(1);

        ResourceRef textureRef = ResourceRef.of(texturePath);
        ResourceRef sheetRef = SpriteSheetResourceLoader.ref(texturePath, sheet);

        resources.loadSync(textureRef, ResourceKind.TEXTURE);
        resources.loadSync(sheetRef, ResourceKind.SPRITE_SHEET);

        assertTrue(resources.isLoaded(textureRef, ResourceKind.TEXTURE));
        assertTrue(resources.isLoaded(sheetRef, ResourceKind.SPRITE_SHEET));

        TextureRegion[] frames = ResourceAccess.spriteSheetFrames(resources, sheetRef);
        TextureRegion frame2 = frames[2];
        assertEquals(2, frame2.getRegionX());
        assertEquals(0, frame2.getRegionY());
        assertEquals(1, frame2.getRegionWidth());
        assertEquals(1, frame2.getRegionHeight());
    }
}
