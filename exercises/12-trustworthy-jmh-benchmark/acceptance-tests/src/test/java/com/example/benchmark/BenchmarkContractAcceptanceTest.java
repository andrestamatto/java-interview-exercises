package com.example.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Tag("acceptance")
class BenchmarkContractAcceptanceTest {
  @Test
  void configuresAMeaningfulMicrobenchmarkContract() throws Exception {
    Class<?> benchmark = OrderLabelBenchmark.class;
    State state = benchmark.getAnnotation(State.class);
    Warmup warmup = benchmark.getAnnotation(Warmup.class);
    Measurement measurement = benchmark.getAnnotation(Measurement.class);
    Fork fork = benchmark.getAnnotation(Fork.class);
    OutputTimeUnit outputUnit = benchmark.getAnnotation(OutputTimeUnit.class);
    Method benchmarkMethod = benchmark.getDeclaredMethod("formatsOrderLabel", Blackhole.class);

    assertThat(state.value()).isEqualTo(Scope.Thread);
    assertThat(warmup.iterations()).isGreaterThanOrEqualTo(2);
    assertThat(measurement.iterations()).isGreaterThanOrEqualTo(3);
    assertThat(fork.value()).isGreaterThanOrEqualTo(2);
    assertThat(outputUnit.value()).isEqualTo(TimeUnit.NANOSECONDS);
    assertThat(benchmark.getAnnotation(org.openjdk.jmh.annotations.BenchmarkMode.class).value())
        .containsExactly(Mode.AverageTime);
    assertThat(benchmark.getDeclaredField("orderNumber").isAnnotationPresent(Param.class)).isTrue();
    assertThat(benchmarkMethod.isAnnotationPresent(Benchmark.class)).isTrue();
  }
}
