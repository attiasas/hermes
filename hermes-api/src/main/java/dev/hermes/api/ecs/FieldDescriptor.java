package dev.hermes.api.ecs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Metadata for a single editable component field in the inspector. */
public final class FieldDescriptor {

  private final String name;
  private final FieldKind kind;
  private final boolean editable;
  private final Double min;
  private final Double max;
  private final Double step;
  private final List<String> enumValues;

  public FieldDescriptor(String name, FieldKind kind, boolean editable) {
    this(name, kind, editable, null, null, null, null);
  }

  public FieldDescriptor(
      String name,
      FieldKind kind,
      boolean editable,
      Double min,
      Double max,
      Double step,
      List<String> enumValues) {
    this.name = name;
    this.kind = kind;
    this.editable = editable;
    this.min = min;
    this.max = max;
    this.step = step;
    this.enumValues =
        enumValues == null ? null : Collections.unmodifiableList(enumValues);
  }

  public String name() {
    return name;
  }

  public FieldKind kind() {
    return kind;
  }

  public boolean editable() {
    return editable;
  }

  public Optional<Double> min() {
    return Optional.ofNullable(min);
  }

  public Optional<Double> max() {
    return Optional.ofNullable(max);
  }

  public Optional<Double> step() {
    return Optional.ofNullable(step);
  }

  public Optional<List<String>> enumValues() {
    return enumValues == null ? Optional.empty() : Optional.of(enumValues);
  }
}
