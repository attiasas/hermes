package dev.hermes.studio.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Line-based read/write of {@code hermes { }} blocks and related Gradle assignments. */
public final class GradleHermesBlockParser {

  private static final Pattern HERMES_BLOCK =
      Pattern.compile("(?ms)^\\s*hermes\\s*\\{.*?^\\s*\\}\\s*$");

  private GradleHermesBlockParser() {}

  public static String readFile(Path file) throws IOException {
    return Files.readString(file);
  }

  public static void writeFile(Path file, String content) throws IOException {
    Files.writeString(file, content);
  }

  public static Optional<String> findHermesBlock(String content) {
    Matcher matcher = HERMES_BLOCK.matcher(content);
    if (matcher.find()) {
      return Optional.of(matcher.group());
    }
    return Optional.empty();
  }

  public static String getStringAssignment(String block, String key) {
    Pattern pattern =
        Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*'([^']*)'\\s*$");
    Matcher matcher = pattern.matcher(block);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static Boolean getBooleanAssignment(String block, String key) {
    Pattern pattern =
        Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*(true|false)\\s*$");
    Matcher matcher = pattern.matcher(block);
    if (!matcher.find()) {
      return null;
    }
    return Boolean.parseBoolean(matcher.group(1));
  }

  public static Integer getIntAssignment(String block, String key) {
    Pattern pattern = Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*(\\d+)\\s*$");
    Matcher matcher = pattern.matcher(block);
    if (!matcher.find()) {
      return null;
    }
    return Integer.parseInt(matcher.group(1));
  }

  public static String getVersionAssignment(String content) {
    Pattern pattern = Pattern.compile("(?m)^\\s*version\\s*=\\s*'([^']*)'\\s*$");
    Matcher matcher = pattern.matcher(content);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static Map<String, Boolean> parsePlatformEnabled(String settingsContent) {
    String block = findHermesBlock(settingsContent).orElse("");
    Map<String, Boolean> enabled = new LinkedHashMap<>();
    for (String platform : new String[] {"desktop", "html", "android"}) {
      String inner = findPlatformInner(block, platform).orElse("");
      Boolean value = getBooleanAssignment(inner, "enabled");
      enabled.put(platform, value == null || value);
    }
    return enabled;
  }

  public static Map<String, int[]> parsePlatformDimensions(String gameBuildContent) {
    String block = findHermesBlock(gameBuildContent).orElse("");
    Map<String, int[]> dimensions = new LinkedHashMap<>();
    for (String platform : new String[] {"desktop", "html", "android"}) {
      String inner = findPlatformInner(block, platform).orElse("");
      Integer width = getIntAssignment(inner, "width");
      Integer height = getIntAssignment(inner, "height");
      if (width != null && height != null) {
        dimensions.put(platform, new int[] {width, height});
      }
    }
    return dimensions;
  }

  public static String setStringAssignment(String block, String key, String value) {
    Pattern pattern =
        Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*'[^']*'\\s*$");
    String replacement = "  " + key + " = '" + value + "'";
    Matcher matcher = pattern.matcher(block);
    if (matcher.find()) {
      return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }
    int insertAt = block.lastIndexOf('}');
    if (insertAt < 0) {
      return block;
    }
    return block.substring(0, insertAt) + "\n" + replacement + "\n" + block.substring(insertAt);
  }

  public static String setBooleanAssignment(String block, String key, boolean value) {
    Pattern pattern =
        Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*(true|false)\\s*$");
    String replacement = "  " + key + " = " + value;
    Matcher matcher = pattern.matcher(block);
    if (matcher.find()) {
      return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }
    int insertAt = block.lastIndexOf('}');
    if (insertAt < 0) {
      return block;
    }
    return block.substring(0, insertAt) + "\n" + replacement + "\n" + block.substring(insertAt);
  }

  public static String setIntAssignmentInPlatform(
      String block, String platform, String key, int value) {
    Optional<String> innerOpt = findPlatformInner(block, platform);
    if (innerOpt.isEmpty()) {
      return block;
    }
    String inner = innerOpt.get();
    Pattern keyPattern =
        Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*\\d+\\s*$");
    String line = "      " + key + " = " + value;
    Matcher keyMatcher = keyPattern.matcher(inner);
    String newInner =
        keyMatcher.find() ? keyMatcher.replaceFirst(Matcher.quoteReplacement(line)) : inner + "\n" + line;
    return replacePlatformInner(block, platform, newInner);
  }

  public static String setPlatformEnabled(String settingsContent, String platform, boolean enabled) {
    String block = findHermesBlock(settingsContent).orElseThrow();
    Optional<String> innerOpt = findPlatformInner(block, platform);
    String newInner = "    enabled = " + enabled;
    String newBlock =
        innerOpt.isPresent()
            ? replacePlatformInner(block, platform, newInner)
            : appendPlatformBlock(block, platform, newInner);
    return settingsContent.replace(block, newBlock);
  }

  public static String replaceHermesBlock(String content, String newBlock) {
    Matcher matcher = HERMES_BLOCK.matcher(content);
    if (matcher.find()) {
      return matcher.replaceFirst(Matcher.quoteReplacement(newBlock));
    }
    return content + "\n\n" + newBlock + "\n";
  }

  public static String setVersionAssignment(String content, String version) {
    Pattern pattern = Pattern.compile("(?m)^\\s*version\\s*=\\s*'[^']*'\\s*$");
    String replacement = "version = '" + version + "'";
    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }
    return replacement + "\n" + content;
  }

  private static Optional<String> findPlatformInner(String block, String platform) {
    Pattern platformPattern =
        Pattern.compile("(?ms)\\b" + Pattern.quote(platform) + "\\b\\s*\\{([^}]*)\\}");
    Matcher matcher = platformPattern.matcher(block);
    return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
  }

  private static String replacePlatformInner(String block, String platform, String newInner) {
    Pattern platformPattern =
        Pattern.compile("(?ms)(\\b" + Pattern.quote(platform) + "\\b\\s*\\{)[^}]*(\\})");
    Matcher matcher = platformPattern.matcher(block);
    if (!matcher.find()) {
      return block;
    }
    return matcher.replaceFirst(
        Matcher.quoteReplacement(matcher.group(1) + "\n" + newInner + "\n  }"));
  }

  private static String appendPlatformBlock(String block, String platform, String inner) {
    Optional<String> platformsInner = findPlatformInner(block, "platforms");
    if (platformsInner.isEmpty()) {
      int insertAt = block.lastIndexOf('}');
      String addition = "  platforms {\n    " + platform + " {\n" + inner + "\n    }\n  }\n";
      return block.substring(0, insertAt) + "\n" + addition + block.substring(insertAt);
    }
    String platformsBlockInner = platformsInner.get();
    String updatedPlatformsInner =
        platformsBlockInner + "\n    " + platform + " {\n" + inner + "\n    }\n";
    return replacePlatformInner(block, "platforms", updatedPlatformsInner);
  }
}
