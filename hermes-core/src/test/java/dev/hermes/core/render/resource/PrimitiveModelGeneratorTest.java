package dev.hermes.core.render.resource;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.hermes.core.TestGdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrimitiveModelGeneratorTest {

    @BeforeAll
    static void initGl() {
        TestGdx.initHeadlessGl();
    }

    @Test
    void boxGenerator_producesModelWithNodes() {
        PrimitiveModelGenerator gen = new PrimitiveModelGenerator();
        Model model = gen.box(2f, 1f, 3f);
        assertNotNull(model);
        assertFalse(model.nodes.isEmpty());
        model.dispose();
    }
}
