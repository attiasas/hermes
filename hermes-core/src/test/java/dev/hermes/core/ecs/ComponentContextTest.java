package dev.hermes.core.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.EntityId;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.Material;
import dev.hermes.api.ecs.Mesh;
import dev.hermes.api.ecs.Transform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ComponentContextTest {

    private ComponentRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistryImpl();
    }

    @Test
    void deserializer_canReadSiblingTransform() {
        registry.register(
                "Follower",
                Follower.class,
                (data, ctx) -> {
                    Follower follower = new Follower();
                    Transform transform = ctx.sibling(Transform.class);
                    follower.setOffsetX(data.getFloat("offsetX", transform != null ? transform.x() : 0f));
                    follower.setOffsetY(data.getFloat("offsetY", transform != null ? transform.y() : 0f));
                    return follower;
                });

        Transform transform = new Transform();
        transform.setX(42f);
        transform.setY(17f);
        ComponentContextImpl context =
                new ComponentContextImpl(new EntityId(1), EntityKind.of("test"), "e1", java.util.Map.of(Transform.class, transform));

        Follower follower =
                (Follower)
                        registry.deserialize(
                                "test/scene.json",
                                "e1",
                                "Follower",
                                new JsonComponentData(new JsonReader().parse("{}")),
                                context);

        assertEquals(42f, follower.offsetX(), 0.001f);
        assertEquals(17f, follower.offsetY(), 0.001f);
    }

    static final class Follower implements dev.hermes.api.Component {
        private float offsetX;
        private float offsetY;

        float offsetX() {
            return offsetX;
        }

        void setOffsetX(float offsetX) {
            this.offsetX = offsetX;
        }

        float offsetY() {
            return offsetY;
        }

        void setOffsetY(float offsetY) {
            this.offsetY = offsetY;
        }
    }
}
