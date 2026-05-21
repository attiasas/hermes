package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.tooling.doctor.HermesDoctorSupport;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class HermesDoctorTest {

  @Test
  void findForbiddenImports_detectsGdxImport() throws Exception {
    Path src =
        Path.of("src/test/resources/bad-import-game/game/src/main/java").toAbsolutePath().normalize();
    List<String> violations = HermesDoctorSupport.findForbiddenImports(src);
    assertEquals(1, violations.size());
    assertTrue(violations.get(0).contains("BadImport.java"));
  }
}
