package com.example.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("acceptance")
class MetricWindowAcceptanceTest {
  @Test
  void preservesValuesAndLongArithmetic() {
    MetricWindow window = new MetricWindow();
    window.add(Integer.MAX_VALUE);
    window.add(Integer.MAX_VALUE);
    assertThat(window.size()).isEqualTo(2);
    assertThat(window.sum()).isEqualTo(4_294_967_294L);
  }
  @Test
  void avoidsBoxingEachSample() {
    MetricWindow window = new MetricWindow();
    BoxingProbe probe = new BoxingProbe();
    window.add(7, probe);
    assertThat(probe.boxedValues()).isZero();
  }
}
