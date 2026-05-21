package dev.hermes.debug;

import java.util.List;

public record WorldSnapshot(long frame, String scenePath, List<EntitySnapshot> entities) {}
