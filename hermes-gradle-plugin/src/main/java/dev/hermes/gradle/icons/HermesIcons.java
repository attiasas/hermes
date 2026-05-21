package dev.hermes.gradle.icons;

import dev.hermes.gradle.dsl.HermesExtension;
import dev.hermes.gradle.internal.HermesAssets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class HermesIcons {

  private static final String RESOURCE_PREFIX = "/hermes-default-icons/";

  private HermesIcons() {}

  static File iconsRoot(Project gameProject, HermesExtension extension) {
    File assets = HermesAssets.resolve(gameProject, extension);
    String subdir = extension.getIcons().getDirectory();
    if (subdir == null || subdir.isBlank()) {
      subdir = "icons";
    }
    return new File(assets, subdir);
  }

  static File resolveIcon(Project gameProject, HermesExtension extension, String relativePath) {
    File assets = HermesAssets.resolve(gameProject, extension);
    File candidate = new File(assets, relativePath);
    if (candidate.isFile()) {
      return candidate;
    }
    return extractFallback(relativePath);
  }

  public static File desktopMac(Project gameProject, HermesExtension extension) {
    return resolveIcon(gameProject, extension, extension.getIcons().getDesktop().getMac());
  }

  public static File desktopWindows(Project gameProject, HermesExtension extension) {
    return resolveIcon(gameProject, extension, extension.getIcons().getDesktop().getWindows());
  }

  static File androidLauncher(Project gameProject, HermesExtension extension) {
    String relative = extension.getIcons().getAndroid().getLauncher();
    File candidate = resolveIcon(gameProject, extension, relative);
    if (isUsableLauncherIcon(candidate)) {
      return candidate;
    }
    return extractFallback(relative);
  }

  private static boolean isUsableLauncherIcon(File icon) {
    if (!icon.isFile()) {
      return false;
    }
    try {
      BufferedImage image = ImageIO.read(icon);
      if (image == null) {
        return false;
      }
      return Math.max(image.getWidth(), image.getHeight()) >= 48;
    } catch (IOException e) {
      return false;
    }
  }

  static File webFavicon(Project gameProject, HermesExtension extension) {
    return resolveIcon(gameProject, extension, extension.getIcons().getWeb().getFavicon());
  }

  private static File extractFallback(String relativePath) {
    String resourcePath = RESOURCE_PREFIX + relativePath;
    try (InputStream in = HermesIcons.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new GradleException("Missing Hermes default icon resource: " + resourcePath);
      }
      File tempDir = Files.createTempDirectory("hermes-icon-").toFile();
      tempDir.deleteOnExit();
      File out = new File(tempDir, new File(relativePath).getName());
      Files.createDirectories(out.getParentFile().toPath());
      Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return out;
    } catch (IOException e) {
      throw new GradleException("Failed to extract default icon " + relativePath, e);
    }
  }
}
