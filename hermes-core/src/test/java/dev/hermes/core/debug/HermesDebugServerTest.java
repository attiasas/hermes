package dev.hermes.core.debug;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

final class HermesDebugServerTest {

  @Test
  void doesNotBindWhenDebugDisabled() {
    DebugRuntime runtime = new DebugRuntime(null, null, () -> false);
    HermesDebugServer server = new HermesDebugServer(runtime, 18765);
    server.startIfEnabled();
    assertFalse(server.isListening());
  }
}
