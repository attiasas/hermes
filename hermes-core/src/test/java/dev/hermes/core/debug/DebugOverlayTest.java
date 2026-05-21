package dev.hermes.core.debug;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

final class DebugOverlayTest {

  @Test
  void disabledWhenDebugFlagFalse() {
    DebugOverlay overlay = new DebugOverlay(() -> false);
    assertFalse(overlay.isActive());
  }
}
