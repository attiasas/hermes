package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonValue;
import dev.hermes.api.Component;
import dev.hermes.api.Entity;
import dev.hermes.api.ecs.EntityKind;
import dev.hermes.api.ecs.EntityStore;
import dev.hermes.api.ecs.EntityTypeRegistry;

import java.util.HashMap;
import java.util.Map;

/** Creates entities by merging type templates with instance component overrides. */
final class EntityFactory {

    private final EntityTypeRegistry types;
    private final ComponentRegistryImpl registry;

    EntityFactory(EntityTypeRegistry types, ComponentRegistryImpl registry) {
        this.types = types;
        this.registry = registry;
    }

    Entity create(
            String sourcePath,
            EntityStore store,
            String name,
            String kind,
            Map<String, JsonValue> instanceComponents) {
        String normalizedKind = kind == null ? "" : kind.trim();
        JsonValue template =
                normalizedKind.isEmpty() || !types.has(normalizedKind)
                        ? emptyObject()
                        : ((EntityTypeDefinitionImpl) types.require(normalizedKind)).componentsJson();
        JsonValue merged =
                ComponentMerge.merge(template, ComponentMerge.toJsonObject(instanceComponents));
        ComponentRefResolver.resolve(sourcePath, name, merged);
        EntityKind entityKind =
                normalizedKind.isEmpty() ? EntityKind.UNSET : EntityKind.of(normalizedKind);
        Entity entity = store.create(name, entityKind);
        Map<Class<? extends Component>, Component> built = new HashMap<>();
        ComponentContextImpl context =
                new ComponentContextImpl(entity.id(), entityKind, name, built, registry.resources());
        for (JsonValue entry : merged) {
            Component component =
                    registry.deserialize(
                            sourcePath, name, entry.name, new JsonComponentData(entry), context);
            built.put(component.getClass(), component);
            store.addComponent(entity.id(), component);
        }
        validateDrawableMaterial(sourcePath, name, merged);
        return entity;
    }

    private static void validateDrawableMaterial(String sourcePath, String entityName, JsonValue merged) {
        boolean hasDrawable = false;
        boolean hasMaterial = false;
        String drawableType = null;
        for (JsonValue entry : merged) {
            String typeName = entry.name;
            if (BuiltinComponents.DRAWABLES.equals(typeName)) {
                hasDrawable = true;
                drawableType = typeName;
            }
            if (BuiltinComponents.MATERIAL.equals(typeName)) {
                hasMaterial = true;
            }
        }
        if (hasDrawable && !hasMaterial) {
            String entityLabel = entityName == null || entityName.isBlank() ? "<unnamed>" : "'" + entityName + "'";
            throw new SceneParseException(
                    "Scene '"
                            + sourcePath
                            + "': entity "
                            + entityLabel
                            + ": "
                            + drawableType
                            + " requires a Material component");
        }
    }

    private static JsonValue emptyObject() {
        return new JsonValue(JsonValue.ValueType.object);
    }
}
