package com.example.lookup;

final class ComparisonCounter {
  private long comparisons;

  public void increment() {
    comparisons++;
  }

  public long comparisons() {
    return comparisons;
  }
}
