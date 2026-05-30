package dev.hermes.api.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime widget tree node: type, id, layout, children, and type-specific properties.
 */
public final class UiNode {

    private final String type;
    private String id;
    private UiLayout layout = new UiLayout();
    private final List<UiNode> children = new ArrayList<>();
    private final Map<String, Object> props = new HashMap<>();

    public UiNode(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Widget type is required");
        }
        this.type = type;
    }

    public String type() {
        return type;
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UiLayout layout() {
        return layout;
    }

    public void setLayout(UiLayout layout) {
        this.layout = layout == null ? new UiLayout() : layout;
    }

    public UiNode layout(UiLayout layout) {
        setLayout(layout);
        return this;
    }

    public static UiNode panel(String id) {
        UiNode node = new UiNode("panel");
        node.setId(id);
        return node;
    }

    public static UiNode label(String id, String text) {
        UiNode node = new UiNode("label");
        node.setId(id);
        node.setProp("text", text);
        return node;
    }

    public List<UiNode> children() {
        return Collections.unmodifiableList(children);
    }

    public void addChild(UiNode child) {
        if (child != null) {
            children.add(child);
        }
    }

    public Map<String, Object> props() {
        return Collections.unmodifiableMap(props);
    }

    public void setProp(String key, Object value) {
        if (key != null && !key.isBlank()) {
            props.put(key, value);
        }
    }

    public Object prop(String key) {
        return props.get(key);
    }
}
