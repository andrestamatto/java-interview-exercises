# Reference Solution

## Rationale

`OrderLabelBenchmark` uses thread-scoped state, parameterized order numbers, and a `Blackhole` that consumes the label. It measures average time in nanoseconds after three warmup iterations, for five measurement iterations, in two isolated JVM forks. The full `jmh` task writes JSON to `build/reports/jmh/results.json`.

Those choices define the operation being measured: format an order label for representative inputs without accidentally measuring cross-thread contention, startup compilation, or an expression the JVM can eliminate. A `Blackhole` is appropriate because the benchmark method is `void`; returning the computed value would also make it observable to JMH.

## Resources and execution cost

The full run executes both `@Param` values in two forks, with one-second warmup and measurement iterations, plus JVM startup and harness overhead. It is intentionally much slower than `jmhSmoke`, whose one short warmup, measurement, and fork exist only to prove that the benchmark can run. Neither task asserts a score: speed is an observation, not a portable contract.

Run the full measurement on a controlled machine:

```shell
./gradlew :exercises:12-trustworthy-jmh-benchmark:solution:jmh
```

Use the smoke run while editing:

```shell
./gradlew :exercises:12-trustworthy-jmh-benchmark:solution:jmhSmoke
```

## Failure handling and recovery

A failed or interrupted JMH run is not valid evidence; correct the environment or benchmark error and create a new result file. Do not merge partial runs or retry only a favorable parameter. If a result conflicts with a production observation, first compare input mix, CPU frequency/power policy, JVM version and flags, allocation/GC, and profiler evidence; then test the end-to-end hypothesis separately.

## Alternatives and trade-offs

- Returning the label is simpler than `Blackhole` for a non-void benchmark, but makes a different method signature.
- `Scope.Benchmark` may be correct for immutable shared fixtures; it would be misleading when the operation is meant to be per-thread.
- Throughput, sample time, or single-shot modes answer different questions from average operation time.
- JFR, async-profiler, and JMH profilers explain a benchmark result; they do not replace a correctly structured benchmark.

## Multi-instance boundary

JMH measures local JVM work. It says nothing about request queueing, connection pools, database behavior, network variance, coordinated load, autoscaling, or tail latency across instances. Use a load test and production telemetry for those system properties.

## Observability and security

Keep the JSON artifact with the JDK distribution/version, JVM flags, CPU model and governor/power mode, OS, input parameters, commit, allocation/GC data, and confidence/error information. Benchmark payloads must be synthetic or approved: do not place customer identifiers, access tokens, or production order data in source, reports, or profiler captures.

## Limits

This benchmark exercises string concatenation only. Its two input values are examples, not a production distribution; it has no score threshold, regression gate, allocation assertion, or tail-latency claim. Forks reduce contamination but cannot eliminate noise from the operating system or hardware.

## Primary references

- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [JMH project](https://openjdk.org/projects/code-tools/jmh/)
- [Java Flight Recorder API](https://docs.oracle.com/en/java/javase/21/jfapi/)
