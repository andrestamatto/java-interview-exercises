package com.example.shutdown;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShutdownSignalTest {
  @Test
  void requestStopChangesTheProcessLocalSignal() {
    ShutdownSignal signal = new ShutdownSignal();

    assertThat(signal.isStopRequested()).isFalse();
    signal.requestStop();
    assertThat(signal.isStopRequested()).isTrue();
  }
}
