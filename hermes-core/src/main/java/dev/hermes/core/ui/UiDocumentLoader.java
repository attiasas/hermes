package dev.hermes.core.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
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

    private static final Set<String> NODE_RESERVED =
            Set.of("type", "id", "layout", "style", "children");

    private final UiWidgetTypes widgetTypes;

    public UiDocumentLoader(UiWidgetTypes widgetTypes) {
        this.widgetTypes = Objects.requireNonNull(widgetTypes, "widgetTypes");
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
            JsonValue root = new JsonReader().parse(json);
            if (root == null || !root.isObject()) {
                throw new UiDocumentParseException("UI document root must be a JSON object");
            }
            int version = root.getInt("version", 0);
            if (version != 1) {
                throw new UiDocumentParseException("\"version\" must be 1");
            }
            JsonValue designSize = requireObject(root, "designSize", "document");
            float designWidth = requireNumber(designSize, "width", "designSize");
            float designHeight = requireNumber(designSize, "height", "designSize");
            JsonValue rootNode = requireObject(root, "root", "document");
            UiNode tree = parseNode(rootNode, "root");
            return new UiDocument(tree, designWidth, designHeight);
        } catch (UiDocumentParseException e) {
            throw e;
        } catch (Exception e) {
            throw new UiDocumentParseException("invalid UI document JSON: " + e.getMessage(), e);
        }
    }

    private UiNode parseNode(JsonValue json, String context) {
        String type = requireString(json, "type", context);
        if (!widgetTypes.supports(type)) {
            throw new UiDocumentParseException(context + ": unknown widget type '" + type + "'");
        }
        UiNode node = new UiNode(type);
        if (json.has("id")) {
            String id = json.getString("id", "").trim();
            if (!id.isEmpty()) {
                node.setId(id);
            }
        }
        JsonValue layoutValue = json.get("layout");
        if (layoutValue != null) {
            if (!layoutValue.isObject()) {
                throw new UiDocumentParseException(context + ": \"layout\" must be an object");
            }
            node.setLayout(parseLayout(layoutValue, context + ".layout"));
        }
        JsonValue styleValue = json.get("style");
        if (styleValue != null) {
            if (!styleValue.isObject()) {
                throw new UiDocumentParseException(context + ": \"style\" must be an object");
            }
            node.setProp("style", jsonObjectToMap(styleValue));
        }
        JsonValue childrenValue = json.get("children");
        if (childrenValue != null) {
            if (!childrenValue.isArray()) {
                throw new UiDocumentParseException(context + ": \"children\" must be an array");
            }
            for (int i = 0; i < childrenValue.size; i++) {
                JsonValue entry = childrenValue.get(i);
                if (!entry.isObject()) {
                    throw new UiDocumentParseException(context + ".children[" + i + "] must be an object");
                }
                node.addChild(parseNode(entry, context + ".children[" + i + "]"));
            }
        }
        for (JsonValue entry : json) {
            String key = entry.name;
            if (NODE_RESERVED.contains(key)) {
                continue;
            }
            node.setProp(key, jsonValueToObject(entry));
        }
        return node;
    }

    private static UiLayout parseLayout(JsonValue layout, String context) {
        UiLayout result = new UiLayout();
        if (layout.has("anchor")) {
            result.setAnchor(parseAnchor(requireString(layout, "anchor", context), context));
        }
        if (layout.has("offsetX")) {
            result.setOffsetX(layout.getFloat("offsetX"));
        }
        if (layout.has("offsetY")) {
            result.setOffsetY(layout.getFloat("offsetY"));
        }
        if (layout.has("width")) {
            result.setWidth(layout.getFloat("width"));
        }
        if (layout.has("height")) {
            result.setHeight(layout.getFloat("height"));
        }
        if (layout.has("paddingLeft")) {
            result.setPaddingLeft(layout.getFloat("paddingLeft"));
        }
        if (layout.has("paddingTop")) {
            result.setPaddingTop(layout.getFloat("paddingTop"));
        }
        if (layout.has("paddingRight")) {
            result.setPaddingRight(layout.getFloat("paddingRight"));
        }
        if (layout.has("paddingBottom")) {
            result.setPaddingBottom(layout.getFloat("paddingBottom"));
        }
        if (layout.has("zIndex")) {
            result.setZIndex(layout.getInt("zIndex"));
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

    private static JsonValue requireObject(JsonValue parent, String field, String context) {
        if (!parent.has(field)) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" is required");
        }
        JsonValue value = parent.get(field);
        if (value == null || !value.isObject()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be an object");
        }
        return value;
    }

    private static String requireString(JsonValue object, String field, String context) {
        if (!object.has(field)) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" is required");
        }
        JsonValue value = object.get(field);
        if (value == null || !value.isString()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be a string");
        }
        String text = value.asString().trim();
        if (text.isEmpty()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be non-empty");
        }
        return text;
    }

    private static float requireNumber(JsonValue object, String field, String context) {
        if (!object.has(field)) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" is required");
        }
        JsonValue value = object.get(field);
        if (value == null || !value.isNumber()) {
            throw new UiDocumentParseException(context + ": \"" + field + "\" must be a number");
        }
        return value.asFloat();
    }

    private static Object jsonValueToObject(JsonValue value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isString()) {
            return value.asString();
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isArray()) {
            return jsonArrayToList(value);
        }
        if (value.isObject()) {
            return jsonObjectToMap(value);
        }
        return null;
    }

    private static List<Object> jsonArrayToList(JsonValue array) {
        List<Object> values = new ArrayList<>(array.size);
        for (int i = 0; i < array.size; i++) {
            values.add(jsonValueToObject(array.get(i)));
        }
        return values;
    }

    private static Map<String, Object> jsonObjectToMap(JsonValue object) {
        Map<String, Object> map = new HashMap<>();
        for (JsonValue entry : object) {
            map.put(entry.name, jsonValueToObject(entry));
        }
        return map;
    }
}
