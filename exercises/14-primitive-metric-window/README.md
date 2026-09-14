# Store Metric Samples Without Boxing

**Category:** JVM memory and data representation  
**Difficulty:** Advanced  
**Estimated time:** 90 minutes

## Scenario, symptoms, and business impact

An ingestion service retains millions of metric samples as `Long` objects. Allocation, object headers, reference indirection, and pointer chasing inflate memory pressure.

## Learning objectives and prerequisites

Replace boxed retention with a dynamically resizing primitive representation while preserving the public aggregate behavior. Complete exercises 12 and 13 first.

## Underlying cause without revealing the solution

Generics cannot hold primitive values. `List<Long>` therefore boxes added `long` values, except where JVM integer caching happens to reuse a wrapper.

## System invariants

- `size()` equals the number of values successfully added.
- `sum()` uses `long` arithmetic and preserves Java's normal `long` overflow semantics.
- Adding a sample does not box that sample into a `Long`.
- The public API remains `add(long)`, `size()`, and `sum()`; storage remains encapsulated.

## Delivery and consistency guarantees

The window is process-local and does not provide concurrent mutation safety, eviction, persistence, or distributed aggregation.

## Process-local versus distributed guarantees

A primitive array changes local representation only. It does not establish aggregation consistency, deduplication, ordering, or exactly-once semantics across instances.

## Functional requirements

Keep `add`, `size`, and `sum`. Use a primitive dynamically resizing array, accumulate in `long`, and satisfy the deterministic boxing probe.

## Non-functional requirements and resource limits

The reference solution includes JMH methods that compare retained `List<Long>` samples with a `MetricWindow` at two sample counts. Its short `jmhSmoke` task enables the GC profiler; the full `jmh` task writes JSON without a profiler. Treat allocation and speed results as environmental evidence; do not use a score threshold.

## Constraints and forbidden shortcuts

Do not retain samples in `List<Long>`, narrow accumulation to `int`, hide boxing in streams, expose mutable storage, or replace the exercise with an off-heap implementation.

## Architecture or sequence diagram

```text
metric sample -> capacity check -> primitive long[] append -> long aggregation scan
```

## Deterministic failure reproduction

The starter records the known boxing site through a package-local acceptance probe. The probe verifies allocation shape without relying on a heap-timing assertion.

## Task and automated acceptance criteria

The implementation must preserve values and `long` arithmetic, grow beyond the initial capacity, and leave the boxing probe at zero. The acceptance test verifies public values and the deterministic probe; compare the JMH baselines manually.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:14-primitive-metric-window:starter:starterAcceptanceTest
./gradlew verifyExercise14
./gradlew :exercises:14-primitive-metric-window:solution:jmhSmoke
./gradlew :exercises:14-primitive-metric-window:solution:jmh
```

## Progressive hints: observation, mechanism, design direction

1. Find where the primitive crosses a generic boundary.
2. Separate contiguous primitive storage from its capacity-growth policy.
3. Keep aggregate behavior independent of storage representation.
4. Check what happens when the initial array is full.

## Common wrong solutions and why they fail

- `List<Long>` still boxes, even if a particular small value happens to use the wrapper cache.
- `int` accumulation overflows sooner and changes the API's arithmetic behavior.
- A fixed-size array fails once the window grows.
- A benchmark without a comparable retention pattern answers a different question.

## Production and emulator boundaries

Choose capacity, eviction, concurrency, and memory limits from workload evidence. Primitive storage is not automatically faster for every access pattern; use JFR/JMH allocation evidence and end-to-end measurements before a production change.

## Specialist extension

Compare primitive arrays, ring buffers, direct buffers, and off-heap ownership. State the eviction, lifecycle, cleanup, and concurrency contract before comparing their memory profiles.

## Interview follow-up questions

- When does boxing matter materially?
- Why is dynamic array growth amortized rather than constant-time per individual append?
- What safety trade-off comes with direct buffers?
- How would this window become concurrent without losing a consistent sum?

## Reflection and future-post evidence

Capture the boxing probe, an initial-capacity growth case, the `long` overflow boundary, and JMH/JFR evidence needed before changing representation in a real service.

## Primary references

- [Java Language Specification: boxing conversion](https://docs.oracle.com/javase/specs/jls/se21/html/jls-5.html#jls-5.1.7)
- [Arrays.copyOf API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html#copyOf(long%5B,int))
- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
