package com.example.scheduler;

final class ScanProbe {
  private int inspectedJobs;

  void inspect() {
    inspectedJobs++;
  }

  int inspectedJobs() {
    return inspectedJobs;
  }
}
