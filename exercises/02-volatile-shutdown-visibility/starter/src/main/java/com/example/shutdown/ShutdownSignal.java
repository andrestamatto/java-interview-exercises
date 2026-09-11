package com.example.shutdown;

public final class ShutdownSignal {
  private boolean stopRequested;

  public void requestStop() {
    stopRequested = true;
  }

  public boolean isStopRequested() {
    return stopRequested;
  }
}
