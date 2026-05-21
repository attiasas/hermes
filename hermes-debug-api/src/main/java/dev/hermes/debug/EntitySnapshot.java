package dev.hermes.debug;

import java.util.List;

public record EntitySnapshot(String id, String name, List<ComponentSnapshot> components) {}
