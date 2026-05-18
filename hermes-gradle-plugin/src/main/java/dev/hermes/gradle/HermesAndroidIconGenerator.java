package dev.hermes.gradle;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.gradle.api.GradleException;

/** Scales a launcher PNG into standard Android {@code mipmap-*} folders. */
final class HermesAndroidIconGenerator {

  private static final Map<String, Integer> MIPMAP_SIZES =
      Map.of(
          "mipmap-mdpi", 48,
          "mipmap-hdpi", 72,
          "mipmap-xhdpi", 96,
          "mipmap-xxhdpi", 144,
          "mipmap-xxxhdpi", 192);

  private HermesAndroidIconGenerator() {}

  static void generateMipmaps(File sourceIcon, File outputRoot) throws IOException {
    BufferedImage source = ImageIO.read(sourceIcon);
    if (source == null) {
      throw new GradleException("Not a readable PNG image: " + sourceIcon.getAbsolutePath());
    }
    int maxEdge = Math.max(source.getWidth(), source.getHeight());
    if (maxEdge < 48) {
      throw new GradleException(
          "Android launcher icon must be at least 48x48 pixels (got "
              + source.getWidth()
              + "x"
              + source.getHeight()
              + "): "
              + sourceIcon.getAbsolutePath());
    }
    for (Map.Entry<String, Integer> entry : new LinkedHashMap<>(MIPMAP_SIZES).entrySet()) {
      File dir = new File(outputRoot, entry.getKey());
      if (!dir.mkdirs() && !dir.isDirectory()) {
        throw new IOException("Failed to create " + dir.getAbsolutePath());
      }
      BufferedImage scaled = scale(source, entry.getValue(), entry.getValue());
      File out = new File(dir, "ic_launcher.png");
      if (!ImageIO.write(scaled, "png", out)) {
        throw new IOException("Failed to write " + out.getAbsolutePath());
      }
    }
  }

  private static BufferedImage scale(BufferedImage source, int width, int height) {
    int type = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    BufferedImage scaled = new BufferedImage(width, height, type);
    Graphics2D graphics = scaled.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.drawImage(source.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
    graphics.dispose();
    return scaled;
  }
}
