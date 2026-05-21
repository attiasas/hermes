package dev.hermes.tooling.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HermesHomeResolverTest {

  @Test
  void isHermesCheckout_requiresApiAndCore(@TempDir Path home) throws Exception {
    Files.createDirectories(home.resolve("hermes-api"));
    assertFalse(HermesHomeResolver.isHermesCheckout(home.toFile()));

    Files.createDirectories(home.resolve("hermes-core"));
    assertTrue(HermesHomeResolver.isHermesCheckout(home.toFile()));
  }

  @Test
  void resolve_readsHermesHomeFromGradleProperties(@TempDir Path projectDir) throws Exception {
    Path home = Files.createDirectories(projectDir.resolve("engine/hermes-api").getParent());
    Files.createDirectories(home.resolve("hermes-api"));
    Files.createDirectories(home.resolve("hermes-core"));
    Files.writeString(
        projectDir.resolve("gradle.properties"),
        "hermes.home=" + home.toAbsolutePath() + "\n",
        StandardCharsets.UTF_8);

    assertEquals(home.toFile().getAbsoluteFile(), HermesHomeResolver.resolve(projectDir));
  }

  @Test
  void resolve_returnsNullWhenUnset(@TempDir Path projectDir) {
    assertNull(HermesHomeResolver.resolve(projectDir));
  }
}
