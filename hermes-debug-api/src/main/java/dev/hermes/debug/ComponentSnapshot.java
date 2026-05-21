package dev.hermes.debug;

import java.util.List;
import java.util.Map;

public record ComponentSnapshot(
    String type, Map<String, Object> properties, List<FieldSnapshot> fields) {}
