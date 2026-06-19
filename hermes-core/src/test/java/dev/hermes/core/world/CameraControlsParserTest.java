package dev.hermes.core.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.input.InputButton;
import org.junit.jupiter.api.Test;

final class CameraControlsParserTest {

    @Test
    void parsesOrbitControlsFromCameraBlock() {
        JsonValue root =
                new JsonReader()
                        .parse(
                                "{\"version\":1,\"projection\":\"perspective\","
                                        + "\"controls\":{\"rotateButton\":\"LEFT\","
                                        + "\"translateButton\":\"RIGHT\",\"forwardButton\":\"MIDDLE\","
                                        + "\"scrollZoom\":true,\"rotateAngle\":360,\"translateUnits\":10}}");
        SceneCameraBlock block = SceneCameraBlockParser.parse("test.json", root);
        assertEquals(InputButton.LEFT, block.controls().rotateButton());
        assertEquals(360f, block.controls().rotateAngle(), 0.001f);
        assertEquals(10f, block.controls().translateUnits(), 0.001f);
    }

    @Test
    void unknownControlsMode_isIgnored() {
        JsonValue root =
                new JsonReader()
                        .parse(
                                "{\"version\":1,\"projection\":\"perspective\","
                                        + "\"controls\":{\"mode\":\"firstPerson\",\"enabled\":false}}");
        SceneCameraBlock block = SceneCameraBlockParser.parse("test.json", root);
        assertFalse(block.controls().enabled());
    }
}
