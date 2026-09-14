package com.example.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class MetricWindowBenchmark {
  @Param({"100", "10000"})
  public int sampleCount;

  @Benchmark
  public List<Long> retainBoxedSamples() {
    List<Long> samples = new ArrayList<>(sampleCount);
    for (int index = 0; index < sampleCount; index++) {
      samples.add(10_000L + index);
    }
    return samples;
  }

  @Benchmark
  public MetricWindow retainPrimitiveSamples() {
    MetricWindow window = new MetricWindow();
    for (int index = 0; index < sampleCount; index++) {
      window.add(10_000L + index);
    }
    return window;
  }
}
