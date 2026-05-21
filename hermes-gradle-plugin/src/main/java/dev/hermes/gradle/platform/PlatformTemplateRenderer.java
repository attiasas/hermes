package dev.hermes.gradle.platform;

import dev.hermes.tooling.template.TemplateEngine;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.gradle.api.GradleException;

/** Renders platform {@code build.gradle} files from bundled {@code .tpl} resources. */
public final class PlatformTemplateRenderer {

  private static final String TEMPLATE_PREFIX = "/hermes-templates/platforms/";

  private PlatformTemplateRenderer() {}

  public static String render(String moduleName, PlatformSyncContext context) {
    String resourcePath = TEMPLATE_PREFIX + moduleName + "/build.gradle.tpl";
    try (InputStream in = PlatformTemplateRenderer.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new GradleException("Missing platform build template: " + resourcePath);
      }
      String template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      return TemplateEngine.substitute(template, context.templateTokens());
    } catch (IOException e) {
      throw new GradleException("Failed to read platform template " + resourcePath, e);
    }
  }
}
