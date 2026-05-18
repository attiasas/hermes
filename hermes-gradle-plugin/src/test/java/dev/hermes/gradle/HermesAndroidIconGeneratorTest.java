package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.gradle.api.GradleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesAndroidIconGeneratorTest {

  @Test
  void generateMipmaps_writesStandardDensities(@TempDir Path temp) throws Exception {
    BufferedImage source = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
    File icon = temp.resolve("source.png").toFile();
    ImageIO.write(source, "png", icon);

    File output = temp.resolve("android-res").toFile();
    HermesAndroidIconGenerator.generateMipmaps(icon, output);

    assertTrue(new File(output, "mipmap-mdpi/ic_launcher.png").isFile());
    assertTrue(new File(output, "mipmap-xxxhdpi/ic_launcher.png").isFile());
    BufferedImage xxxhdpi = ImageIO.read(new File(output, "mipmap-xxxhdpi/ic_launcher.png"));
    assertEquals(192, xxxhdpi.getWidth());
    assertEquals(192, xxxhdpi.getHeight());
    assertTrue(Files.size(new File(output, "mipmap-xxxhdpi/ic_launcher.png").toPath()) > 200);
  }

  @Test
  void generateMipmaps_rejectsTinySource(@TempDir Path temp) throws Exception {
    BufferedImage tiny = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    File icon = temp.resolve("tiny.png").toFile();
    ImageIO.write(tiny, "png", icon);
    org.junit.jupiter.api.Assertions.assertThrows(
        GradleException.class,
        () -> HermesAndroidIconGenerator.generateMipmaps(icon, temp.resolve("out").toFile()));
  }
}
