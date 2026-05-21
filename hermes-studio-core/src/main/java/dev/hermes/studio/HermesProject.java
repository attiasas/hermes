package dev.hermes.studio;

import java.nio.file.Path;

/** Opened Hermes game workspace (Gradle root + game module). */
public record HermesProject(Path root, Path gameDir, Path hermesJson, Path assetsDir) {}
