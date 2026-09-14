package com.example.metrics;

final class BoxingProbe {
  private int boxedValues;

  void recordBoxing() {
    boxedValues++;
  }

  int boxedValues() {
    return boxedValues;
  }
}
