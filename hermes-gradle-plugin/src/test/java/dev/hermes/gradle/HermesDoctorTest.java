package dev.hermes.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.hermes.tooling.doctor.HermesDoctorSupport;
import dev.hermes.tooling.doctor.HermesDoctorSupport.CheckResult;
import dev.hermes.tooling.doctor.HermesDoctorSupport.Status;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class HermesDoctorTest {

  @Test
  void checkEngineResolution_siblingHermesApi_isOk() {
    CheckResult result =
        HermesDoctorSupport.checkEngineResolution(true, false, null, "0.1.0-SNAPSHOT");
    assertEquals(Status.OK, result.status());
    assertTrue(result.message().contains("sibling"));
  }

  @Test
  void checkEngineResolution_hermesHomeOnly_isWarn() {
    CheckResult result =
        HermesDoctorSupport.checkEngineResolution(
            false, true, new File("/tmp/hermes-checkout"), "0.1.0-SNAPSHOT");
    assertEquals(Status.WARN, result.status());
    assertTrue(result.message().contains("HERMES_HOME"));
    assertTrue(result.message().contains("Launchers can sync"));
  }

  @Test
  void checkEngineResolution_noResolution_isFail() {
    CheckResult result =
        HermesDoctorSupport.checkEngineResolution(false, false, null, "nonexistent-version-xyz");
    assertEquals(Status.FAIL, result.status());
    assertTrue(result.message().contains("Maven local"));
  }

  @Test
  void findForbiddenImports_detectsGdxImport() throws Exception {
    Path src =
        Path.of("src/test/resources/bad-import-game/game/src/main/java").toAbsolutePath().normalize();
    List<String> violations = HermesDoctorSupport.findForbiddenImports(src);
    assertEquals(1, violations.size());
    assertTrue(violations.get(0).contains("BadImport.java"));
  }
}
