# Reference Solution

## Rationale

`MetricWindow` stores values in a `long[]` that starts with capacity 16. `append` doubles the array with `Arrays.copyOf` when it is full, writes the primitive value, and increments `size`. `sum` iterates from zero to `size` using a `long` accumulator. The package-local probe overload calls the same append path and deliberately does not record boxing.

This removes one `Long` wrapper and one reference slot per normally added value while keeping `add(long)`, `size()`, and `sum()` unchanged. It also makes the storage and growth policy visible rather than hiding it inside a generic collection.

## Resources and complexity

Appending is amortized `O(1)`: a resize costs `O(n)` copying for that call, but doubling spreads copying across many appends. `size` is `O(1)` and `sum` is `O(n)`. Storage is `O(capacity)`, not exactly `O(size)`; during a resize, both old and new arrays temporarily coexist. The array does not shrink and, like other Java arrays, is ultimately bounded by addressable `int` length and available heap.

The included JMH benchmark retains `List<Long>` samples and primitive-window samples for counts 100 and 10,000. Its returned objects make the retained work observable to JMH. The short `jmhSmoke` task enables the GC profiler for allocation evidence, whereas the full `jmh` task writes JSON without a profiler. Values in the small `Long` cache can reduce the boxed baseline's allocation for a subset of samples, so interpret the result as this benchmark's workload, not a universal byte-per-value law.

## Failure handling and recovery

There is no external side effect to roll back. A capacity expansion can fail with `OutOfMemoryError`; production recovery is admission control, eviction, bounded retention, or reconstructing aggregates from a durable source—not catching an error and pretending the sample was retained. `long` addition intentionally wraps on overflow according to Java arithmetic; callers requiring overflow detection need a different contract, such as `Math.addExact` and an explicit failure policy.

## Alternatives and trade-offs

- A ring buffer bounds memory and supports a moving window, but changes the meaning of `size` and `sum` because old samples are evicted.
- Primitive collections libraries can reduce implementation work, at the cost of a dependency and their own API/lifecycle choices.
- `LongBuffer` or off-heap storage can shift heap pressure, but introduces explicit ownership, cleanup, access, and observability concerns.
- A maintained running sum makes `sum` `O(1)`, but needs a specified overflow and concurrent-update policy.

## Multi-instance boundary

This object owns data in one heap only. It neither merges shards nor prevents duplicate samples. A distributed metric pipeline needs partitioning, aggregation windows, deduplication, lateness handling, and a durable/replay strategy independent of this representation.

## Observability and security

Monitor retained-sample count, configured capacity/eviction, heap occupancy, allocation rate, GC pauses, and ingestion rejection; do not expose the mutable array for diagnostics. Metric tags and values can reveal tenant, customer, or operational information, so apply cardinality limits, access controls, and redaction at the metrics boundary. The deterministic probe is a test aid, not a production allocation meter.

## Limits

The class is not thread-safe, has no removal or eviction operation, retains all values until the object becomes unreachable, and makes `sum` scan them each time. It does not detect overflow, persist samples, validate input, or offer a memory cap. The JMH comparison measures allocation and local runtime, not distributed ingestion performance.

## Primary references

- [Java Language Specification: boxing conversion](https://docs.oracle.com/javase/specs/jls/se21/html/jls-5.html#jls-5.1.7)
- [Arrays.copyOf API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html#copyOf(long%5B,int))
- [Java Language Specification: integer operators](https://docs.oracle.com/javase/specs/jls/se21/html/jls-15.html#jls-15.18.2)
- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
