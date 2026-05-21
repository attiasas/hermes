package dev.hermes.api.ecs;

import java.util.List;

/** Declares inspector field metadata for a registered component type. */
public interface ComponentInspectorDescriptor {

  List<FieldDescriptor> fields();
}
