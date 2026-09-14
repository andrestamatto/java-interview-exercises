package com.example.metrics;

import java.util.Arrays;

public final class MetricWindow {
  private long[] values = new long[16];
  private int size;

  public void add(long value) {
    append(value);
  }

  void add(long value, BoxingProbe probe) {
    append(value);
  }

  private void append(long value) {
    if (size == values.length) {
      values = Arrays.copyOf(values, values.length * 2);
    }
    values[size++] = value;
  }

  public long sum() {
    long total = 0;
    for (int index = 0; index < size; index++) {
      total += values[index];
    }
    return total;
  }

  public int size() {
    return size;
  }
}
