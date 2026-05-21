package dev.hermes.debug;

public record FieldSnapshot(String name, FieldKind kind, boolean editable, Object value) {}
