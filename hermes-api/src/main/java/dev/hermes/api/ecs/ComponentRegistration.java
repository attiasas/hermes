package dev.hermes.api.ecs;

import dev.hermes.api.Component;

/** ServiceLoader entry for automatic component and system registration. */
public interface ComponentRegistration {

  void register(HermesEngine engine);
}
