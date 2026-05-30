package dev.hermes.core.ui;

import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.hermes.api.ui.UiAnchor;
import dev.hermes.api.ui.UiDocument;
import dev.hermes.api.ui.UiLayout;
import dev.hermes.api.ui.UiNode;
import dev.hermes.core.HermesAssetPaths;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Loads {@link UiDocument} instances from game asset paths. */
public final class UiDocumentLoader {

    private static final Gson GSON = new Gson();
    private static final Set<String> NODE_RESERVED =
            Set.of("type", "id", "layout", "style", "children");

    private final BuiltinUiWidgets builtins;

    public UiDocumentLoader(BuiltinUiWidgets builtins) {
        this.builtins = Objects.requireNonNull(builtins, "builtins");
    }

    public UiDocument load(String assetPath) {
        if (assetPath == null || assetPath.isBlank()) {
            throw new UiDocumentParseException("UI document asset path is required");
        }
        FileHandle handle = HermesAssetPaths.internal(assetPath);
        if (!handle.exists()) {
            throw new UiDocumentParseException("UI document not found: " + assetPath);
        }
        return parse(handle.readString(StandardCharsets.UTF_8.name()));
    }

    public UiDocument parse(String json) {
        try {
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) {
                throw new UiDocumentParseException("UI document root must be a JSON object");
            }
            int version = root.has("version") ? root.get("version").getAsInt() : 0;
            if (version != 1) {
                throw new UiDocumentParseException("\"version\" must be 1");
            }
            JsonObject designSize = requireObject(root, "designSize", "document");
            float designWidth = requireNumber(designSize, "width", "designSize");
            float designHeight = requireNumber(designSize, "height", "designSize");
            JsonObject rootNode = requireObject(root, "root", "document");
            UiNode tree = parseNode(rootNode, "root");
            return new UiDocument(tree, designWidth, designHeight);
        } catch (UiDocumentParseException e) {
            throw e;
        } catch (Exception e) {
            throw new UiDocumentParseException("invalid UI document JSON: " + e.getMessage(), e);
        }
    }

    private UiNode parseNode(JsonObject json, String context) {
        String type = requireString(json, "type", context);
        if (!builtins.supports(type)) {
            throw new UiDocumentParseException(context + ": unknown widget type '" + type + "'");
        }
        UiNode node = new UiNode(type);
        if (json.has("id")) {
            String id = json.get("id").getAsString();
            if (id != null && !id.isBlank()) {
                node.setId(id);
            }
        }
        if (json.has("layout")) {
            JsonElement layoutValue = json.get("layout");
            if (!layoutValue.isJsonObject()) {
                throw new UiDocumentParseException(context + ": \"layout\" must be an object");
            }
            node.setLayout(parseLayout(layoutValue.getAsJsonObject(), context + ".layout"));
        }
        if (json.has("style")) {
            JsonElement styleValue = json.get("style");
            if (!styleValue.isJsonObject()) {
                throw new UiDocumentParseException(context + ": \"style\" must be an object");
            }
            node.setProp("style", jsonObjectToMap(styleValue.getAsJsonObject()));
        }
        if (json.has("children")) {
            JsonElement childrenValue = json.get("children");
            if (!childrenValue.isJsonArray()) {
                throw new UiDocumentParseException(context + ": \"children\" must be an array");
            }
            JsonArray children = childrenValue.getAsJsonArray();
            for (int i = 0; i < children.size(); i++) {
                JsonElement entry = children.get(i);
                if (!entry.isJsonObject()) {
                    throw new UiDocumentParseException(context + ".children[" + i + "] must be an object");
                }
                node.addChild(parseNode(entry.getAsJsonObject(), context + ".children[" + i + "]"));
            }
        }
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            if (NODE_RESERVED.contains(key)) {
                continue;
            }
            node.setProp(key, jsonElementToValue(entry.getValue()));
        }
        return node;
    }

    private static UiLayout parseLayout(JsonObject layout, String context) {
        UiLayout result = new UiLayout();
        if (layout.has("anchor")) {
            result.setAnchor(parseAnchor(requireString(layout, "anchor", context), context));
        }
        if (layout.has("offsetX")) {
            result.setOffsetX(layout.get("offsetX").getAsFloat());
        }
        if (layout.has("offsetY")) {
            result.setOffsetY(layout.get("offsetY").getAsFloat());
        }
        if (layout.has("width")) {
            result.setWidth(layout.get("width").getAsFloat());
        }
        if (layout.has("height")) {
            result.setHeight(layout.get("height").getAsFloat());
        }
        if (layout.has("paddingLeft")) {
            result.setPaddingLeft(layout.get("paddingLeft").getAsFloat());
        }
        if (layout.has("paddingTop")) {
            result.setPaddingTop(layout.get("paddingTop").getAsFloat());
        }
        if (layout.has("paddingRight")) {
            result.setPaddingRight(layout.get("paddingRight").getAsFloat());
        }
        if (layout.has("paddingBottom")) {
            result.setPaddingBottom(layout.get("paddingBottom").getAsFloat());
        }
        if (layout.has("zIndex")) {
            result.setZIndex(layout.get("zIndex").getAsInt());
        }
        return result;
    }

    private static UiAnchor parseAnchor(String anchorName, String context) {
        if (anchorName == null || anchorName.isBlank()) {
            throw new UiDocumentParseException(context + ": \"anchor\" must be non-empty");
        }
        switch (anchorName) {
            case "topLeft":
                return UiAnchor.TOP_LEFT;
            case "topCenter":
                return UiAnchor.TOP_CENTER;
            case "topRight":
                return UiAnchor.TOP_RIGHT;
            case "centerLeft":
                return UiAnchor.CENTER_LEFT;
            case "center":
                return UiAnchor.CENTER;
            case "centerRight":
                return UiAnchor.CENTER_RIGHT;
            case "bottomLeft":
                return UiAnchor.BOTTOM_LEFT;
            case "bottomCenter":
                return UiAnchor.BOTTOM_CENTER;
            case "bottomRight":
                return UiAnchor.BOTTOM_RIGHT;
            case "stretch":
                return UiAnchor.STRETCH;
            default:
                throw new UiDocumentParseException(context + ": unknown anchor '" + anchorName + "'");
        }
    }

    private static JsonObject requireObject(JsonObject parent, String field, String context) {
        if (!parent.has(field)) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" is required");
        }
        JsonElement value = parent.get(field);
        if (!value.isJsonObject()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be an object");
        }
        return value.getAsJsonObject();
    }

    private static String requireString(JsonObject object, String field, String context) {
        if (!object.has(field)) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" is required");
        }
        JsonElement value = object.get(field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be a string");
        }
        String text = value.getAsString().trim();
        if (text.isEmpty()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be non-empty");
        }
        return text;
    }

    private static float requireNumber(JsonObject object, String field, String context) {
        if (!object.has(field)) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" is required");
        }
        JsonElement value = object.get(field);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be a number");
        }
        return value.getAsFloat();
    }

    private static Object jsonElementToValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isString()) {
                return primitive.getAsString();
            }
            if (primitive.isNumber()) {
                return primitive.getAsDouble();
            }
            return null;
        }
        if (element.isJsonArray()) {
            return jsonArrayToList(element.getAsJsonArray());
        }
        if (element.isJsonObject()) {
            return jsonObjectToMap(element.getAsJsonObject());
        }
        return null;
    }

    private static List<Object> jsonArrayToList(JsonArray array) {
        List<Object> values = new ArrayList<>(array.size());
        for (JsonElement entry : array) {
            values.add(jsonElementToValue(entry));
        }
        return values;
    }

    private static Map<String, Object> jsonObjectToMap(JsonObject object) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            map.put(entry.getKey(), jsonElementToValue(entry.getValue()));
        }
        return map;
    }
}
