package dev.hermes.tooling.template;

import java.util.Map;

/** Simple {@code {{token}}} substitution for Hermes project templates. */
public final class TemplateEngine {

  private TemplateEngine() {}

  public static String substitute(String input, Map<String, String> tokens) {
    if (input == null || tokens == null || tokens.isEmpty()) {
      return input;
    }
    String result = input;
    for (Map.Entry<String, String> entry : tokens.entrySet()) {
      result = result.replace(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
