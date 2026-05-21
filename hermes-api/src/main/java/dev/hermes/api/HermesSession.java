package dev.hermes.api;

/**
 * Per-game session state shared across scenes (player profile, settings, progression).
 *
 * <p>Future: audio mixer state, save slots, and cross-scene services will live here.
 */
public interface HermesSession {

  /** No-op session used until session services are implemented. */
  HermesSession EMPTY = new HermesSession() {};
}
