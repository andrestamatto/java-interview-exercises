package com.example.parser;

final class ParserAllocationProbe {
  private int intermediateArrays;

  void recordIntermediateArray() {
    intermediateArrays++;
  }

  int intermediateArrays() {
    return intermediateArrays;
  }
}
