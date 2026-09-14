package com.example.metrics;

import java.util.ArrayList;
import java.util.List;

public final class MetricWindow {
  private final List<Long> values = new ArrayList<>();

  public void add(long value) {
    add(value, new BoxingProbe());
  }

  void add(long value, BoxingProbe probe) {
    probe.recordBoxing();
    values.add(value);
  }

  public long sum() {
    return values.stream().mapToLong(Long::longValue).sum();
  }

  public int size() {
    return values.size();
  }
}
