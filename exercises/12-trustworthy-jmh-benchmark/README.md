# Build a Trustworthy JMH Benchmark

**Category:** Performance measurement  
**Difficulty:** Advanced  
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

A team selected a formatter after a `nanoTime` loop reported a dramatic win. Production showed no improvement because the loop measured warmup and allowed the result to disappear.

## Learning objectives and prerequisites

Configure a microbenchmark so it measures a defined operation. Complete exercise 11 first.

## Underlying cause without revealing the solution

The JVM optimizes, compiles, and collects during a benchmark. A loop without a harness can measure setup or dead-code elimination rather than useful work.

## System invariants

- Benchmark state has an explicit scope.
- Every measured result is observable to JMH.
- Inputs, units, warmup, measurement, forks, and output artifact are explicit.

## Delivery and consistency guarantees

JMH provides controlled JVM measurement, not a production latency SLO or a universal winner.

## Process-local versus distributed guarantees

This measures local JVM work only. It does not model network, database, queueing, or multi-instance behavior.

## Functional requirements

Repair `OrderLabelBenchmark` until the structural acceptance contract passes and retain JSON output for the full run.

## Non-functional requirements and resource limits

The smoke task is deliberately short. Use the full task for manual analysis and never assert a score threshold in CI.

## Constraints and forbidden shortcuts

Do not replace JMH with elapsed-time loops, consume a constant, use zero warmup, or remove parameters to make one result look better.

## Architecture or sequence diagram

```text
@Param input -> @State(Thread) -> @Benchmark -> Blackhole -> JMH result JSON
```

## Deterministic failure reproduction

The starter acceptance test inspects the benchmark contract rather than timing a machine.

## Task and automated acceptance criteria

Use thread state, average nanoseconds, parameterized input, Blackhole consumption, at least two warmups, three measurements, two forks, and JSON output.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:12-trustworthy-jmh-benchmark:starter:starterAcceptanceTest
./gradlew verifyExercise12
./gradlew :exercises:12-trustworthy-jmh-benchmark:solution:jmhSmoke
./gradlew :exercises:12-trustworthy-jmh-benchmark:solution:jmh
```

## Progressive hints: observation, mechanism, design direction

1. List what the JVM can optimize away.
2. Make the result and input visible to JMH.
3. Separate quick smoke execution from a repeatable full run.

## Common wrong solutions and why they fail

- A `nanoTime` loop has no warmup/fork isolation.
- A shared state can measure contention instead of formatting.
- One benchmark score is not a portable performance guarantee.

## Production and emulator boundaries

Record CPU, JVM, flags, input mix, allocation, GC, and error bars before comparing runs. Profile end-to-end workloads separately.

## Specialist extension

Use JFR and JMH profilers to explain allocation and compilation effects, then validate a production hypothesis with load evidence.

## Interview follow-up questions

- Why are forks valuable?
- When is `Blackhole` unnecessary?
- Which result mode fits a tail-latency question?

## Reflection and future-post evidence

Capture the bad structural contract, the smoke command, and the metadata required to compare two benchmark files credibly.

## Primary references

- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
