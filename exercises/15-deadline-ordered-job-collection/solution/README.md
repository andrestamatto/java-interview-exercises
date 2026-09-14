# Reference Solution

## Rationale

The solution keeps two representations of a scheduled job:

- `activeById` is the authoritative map of currently active job definitions.
- `jobs` is a `PriorityQueue` ordered by deadline and then ID.

Scheduling writes the current definition to the map and adds it to the heap. Cancelling removes only the map entry. Polling removes heap heads until `activeById.remove(job.id(), job)` succeeds; that conditional removal proves that the polled record is still the current definition for that ID. A cancelled record or a record superseded by a later schedule no longer equals the map value, so it is safely discarded.

This is lazy stale-entry cleanup. It avoids the linear arbitrary-element removal offered by `PriorityQueue.remove(Object)` while preserving deterministic `(deadlineMillis, id)` order among active jobs. The jqwik property test compares mixed schedule, cancel, and poll operations to a simple map-based reference model, including replacement of an existing ID.

## Resources and complexity

Let `h` be heap entries, including stale ones. `schedule` is `O(log h)` for heap insertion plus expected `O(1)` map replacement; `cancel` is expected `O(1)` because it only changes the map. An ordinary `poll` that finds a current head is `O(log h)`.

There is no strict `O(log h)` bound for every individual poll. If `k` stale entries have accumulated at the heap head after a cancel-heavy or reschedule-heavy sequence, that poll does `k + 1` heap removals: `O((k + 1) log h)`. Across a sequence, each heap entry is discarded at most once, which gives an amortized cleanup argument, but it does not protect a latency-sensitive caller from one cancellation burst. Stale entries also consume memory until polling reaches them; their number is not bounded by the number of active jobs.

The JMH suite runs steady `pollAndReschedule` and a `cancelScheduleAndPoll` workload for 100 and 10,000 active jobs. Its short `jmhSmoke` task enables the GC profiler; its full `jmh` task writes JSON without a profiler. Both are useful for comparing local workload shapes, not for establishing a scheduler latency SLO.

## Failure handling and recovery

The collection has no persistence. A JVM crash loses active jobs, stale entries, and cancellation history; recovery in a real scheduler requires replaying a durable source under a lease/fencing and idempotency protocol. A cancellation burst can create a slow cleanup poll but does not return a cancelled/superseded job. If that latency is unacceptable, change the data structure or introduce a bounded cleanup policy rather than relying on this class to recover it.

## Alternatives and trade-offs

- An indexed heap maintains each ID's heap position, giving exact `O(log n)` cancellation/removal but substantially more mutation invariants.
- A `TreeSet` ordered by deadline/ID plus an ID map permits exact removal in `O(log n)`, with different memory and comparator semantics.
- `DelayQueue` adds blocking delay-oriented consumption but does not provide durable ownership or a complete ID-index/cancellation protocol.
- A database-backed scheduler can make state durable, but needs transactional ownership, indexing, contention, and clock reasoning.

## Multi-instance boundary

This object is neither thread-safe nor shared between JVMs. Distributed schedulers need one durable source of truth, leader/partition ownership, fencing tokens to prevent a former owner from dispatching, idempotent job handlers, retries, and a documented clock policy. Local queue order does not prevent duplicate execution after failover.

## Observability and security

Monitor active-map size, heap size, stale-to-active ratio, cancellation/replacement rate, number of stale entries discarded per poll, poll latency, and dispatch lag. The implementation exposes none of these counters, so collect them in the scheduler boundary rather than leaking internals. Validate job IDs and deadlines at that boundary, apply quotas to prevent unbounded heap growth, authorize cancellation, and avoid placing sensitive job payloads in IDs, traces, or logs.

## Limits

The API does not validate null jobs, IDs, or deadline ranges; callers must supply values compatible with the comparator. There is no blocking `poll`, clock wait, capacity limit, eviction, persistence, retry, payload, or concurrency control. Lazy cleanup can retain an unbounded stale backlog until polling occurs and can make a single post-burst poll expensive. The JMH and jqwik tests improve confidence in local behavior but do not prove concurrent or distributed correctness.

## Primary references

- [PriorityQueue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)
- [Map.remove(key, value) API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html#remove(java.lang.Object,java.lang.Object))
- [HashMap API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html)
- [jqwik user guide](https://jqwik.net/docs/current/user-guide.html)
