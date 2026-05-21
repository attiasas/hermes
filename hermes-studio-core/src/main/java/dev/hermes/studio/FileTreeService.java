package dev.hermes.studio;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Builds a simple project file tree for the Studio files panel. */
public final class FileTreeService {

  private static final Set<String> SKIP_DIRS =
      Set.of(".git", ".gradle", "build", "node_modules", "dist", ".idea", "out");

  public List<FileNode> list(Path root) throws IOException {
    Path projectRoot = root.toAbsolutePath().normalize();
    List<FileNode> nodes = new ArrayList<>();
    Files.walkFileTree(
        projectRoot,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (!dir.equals(projectRoot) && SKIP_DIRS.contains(dir.getFileName().toString())) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            if (!dir.equals(projectRoot)) {
              nodes.add(node(projectRoot, dir, true));
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            nodes.add(node(projectRoot, file, false));
            return FileVisitResult.CONTINUE;
          }
        });
    nodes.sort(Comparator.comparing(FileNode::path));
    return nodes;
  }

  private static FileNode node(Path root, Path path, boolean directory) {
    return new FileNode(root.relativize(path).toString().replace('\\', '/'), directory);
  }

  public record FileNode(String path, boolean directory) {}
}
