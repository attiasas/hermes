package dev.hermes.core.ecs;

import com.badlogic.gdx.utils.JsonValue;

/** Resolves sibling-component {@code $ref} placeholders on merged entity JSON before deserialize. */
final class ComponentRefResolver {

    private ComponentRefResolver() {
    }

    static void resolve(String sourcePath, String entityName, JsonValue merged) {
        if (merged == null || !merged.isObject()) {
            return;
        }
        for (JsonValue component : merged) {
            if (!component.isObject()) {
                continue;
            }
            for (JsonValue property : component) {
                if (!isRef(property)) {
                    continue;
                }
                String ref = property.getString("$ref");
                float resolved =
                        resolveTransformRef(sourcePath, entityName, merged, component.name, property.name, ref);
                component.remove(property.name);
                component.addChild(property.name, new JsonValue(resolved));
            }
        }
    }

    private static boolean isRef(JsonValue value) {
        if (!value.isObject()) {
            return false;
        }
        JsonValue child = value.child;
        if (child == null || child.next != null) {
            return false;
        }
        return "$ref".equals(child.name) && child.isString();
    }

    private static float resolveTransformRef(
            String sourcePath,
            String entityName,
            JsonValue merged,
            String componentType,
            String propertyName,
            String ref) {
        if (ref == null || ref.isBlank()) {
            throw unresolvedRef(sourcePath, entityName, componentType + "." + propertyName, ref);
        }
        int dot = ref.indexOf('.');
        if (dot <= 0 || dot == ref.length() - 1) {
            throw unresolvedRef(sourcePath, entityName, componentType + "." + propertyName, ref);
        }
        String targetComponent = ref.substring(0, dot);
        String targetField = ref.substring(dot + 1);
        if (!BuiltinComponents.TRANSFORM.equals(targetComponent)) {
            throw unresolvedRef(sourcePath, entityName, componentType + "." + propertyName, ref);
        }
        JsonValue transform = merged.get(BuiltinComponents.TRANSFORM);
        if (transform == null || !transform.isObject()) {
            throw unresolvedRef(sourcePath, entityName, componentType + "." + propertyName, ref);
        }
        if ("x".equals(targetField)) {
            return transform.getFloat("x", 0f);
        }
        if ("y".equals(targetField)) {
            return transform.getFloat("y", 0f);
        }
        if ("z".equals(targetField)) {
            return transform.getFloat("z", 0f);
        }
        throw unresolvedRef(sourcePath, entityName, componentType + "." + propertyName, ref);
    }

    private static SceneParseException unresolvedRef(
            String sourcePath, String entityName, String path, String ref) {
        String entityLabel = entityName == null || entityName.isBlank() ? "<unnamed>" : "'" + entityName + "'";
        return new SceneParseException(
                "Scene '"
                        + sourcePath
                        + "': entity "
                        + entityLabel
                        + ": unresolved $ref '"
                        + ref
                        + "' at "
                        + path);
    }
}
