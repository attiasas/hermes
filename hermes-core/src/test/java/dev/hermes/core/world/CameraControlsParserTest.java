package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.world.CameraControlsMode;
import org.junit.jupiter.api.Test;

final class CameraControlsParserTest {

    @Test
    void parsesOrbitControlsFromCameraBlock() {
        JsonValue root =
                new JsonReader()
                        .parse(
                                "{\"version\":1,\"projection\":\"perspective\","
                                        + "\"controls\":{\"mode\":\"orbit\",\"rotateButton\":\"LEFT\","
                                        + "\"translateButton\":\"RIGHT\",\"forwardButton\":\"MIDDLE\","
                                        + "\"scrollZoom\":true,\"rotateAngle\":360,\"translateUnits\":10}}");
        SceneCameraBlock block = SceneCameraBlockParser.parse("test.json", root);
        assertEquals(CameraControlsMode.ORBIT, block.controls().mode());
        assertEquals(360f, block.controls().rotateAngle(), 0.001f);
        assertEquals(10f, block.controls().translateUnits(), 0.001f);
    }

    @Test
    void parsesFirstPersonMode() {
        JsonValue root =
                new JsonReader()
                        .parse(
                                "{\"version\":1,\"projection\":\"perspective\","
                                        + "\"controls\":{\"mode\":\"firstPerson\",\"velocity\":8,"
                                        + "\"degreesPerPixel\":0.5}}");
        SceneCameraBlock block = SceneCameraBlockParser.parse("test.json", root);
        assertEquals(CameraControlsMode.FIRST_PERSON, block.controls().mode());
        assertEquals(8f, block.controls().velocity(), 0.001f);
    }
}
