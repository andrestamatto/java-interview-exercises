# Maintain Deadline-Ordered Jobs

**Category:** Data structures and performance  
**Difficulty:** Advanced  
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

A scheduler scans an `ArrayList` to find each next deadline. Queue growth turns routine dispatch into CPU work, and removing a list element shifts later elements.

## Learning objectives and prerequisites

Model deadline ordering, identity, replacement, and cancellation separately. Complete exercises 11–14 first.

## Underlying cause without revealing the solution

An unsorted list makes minimum selection linear; deleting a list element can shift all remaining values. A heap solves minimum retrieval but not identity-based cancellation by itself.

## System invariants

- Active jobs poll in `(deadlineMillis, id)` order.
- At most one job per ID is active. Scheduling an existing ID replaces its active definition; older heap entries for that ID become stale.
- `cancel(id)` returns `true` only when an active job was removed, so repeated cancellation is idempotent.
- A cancelled or superseded job is never returned.

## Delivery and consistency guarantees

This is a single-process collection, not a durable scheduler, delay service, retry engine, or distributed lease.

## Process-local versus distributed guarantees

Multiple instances need durable ownership, fencing, retry/idempotency, and clock policy outside this exercise. Heap order in one JVM is not a distributed dispatch guarantee.

## Functional requirements

Keep `schedule`, `cancel`, and `poll`; pass ordering, replacement, cancellation, and deterministic scan-probe tests. Use a deadline-ordered heap plus a separate active-ID index, then remove stale heap entries only when reached by `poll`.

## Non-functional requirements and resource limits

Avoid the starter's explicit full active-list scan on every poll. A normal heap poll is logarithmic, but lazy deletion has an important limit: after many cancellations or replacements, one `poll` may discard many stale entries and therefore be linear in that stale backlog (each removal also costs heap work). Benchmark the actual schedule/cancel/poll ratio and stale-entry accumulation rather than promising a hard per-poll bound.

## Constraints and forbidden shortcuts

Do not sort the list on every call, remove cancellation semantics, use wall-clock assertions, or claim that lazy cleanup gives strict `O(log n)` latency for every individual `poll`.

## Architecture or sequence diagram

```text
schedule -> priority queue + active-ID map
cancel   -> remove from active-ID map
poll     -> discard stale heap heads -> return earliest current active job
```

## Deterministic failure reproduction

The starter acceptance test counts its explicit full queue scan instead of measuring elapsed time. The test cannot measure all lazy-cleanup costs, so use the JMH workload and reason about cancel-heavy cases separately.

## Task and automated acceptance criteria

Preserve deadline-then-ID ordering, replacement semantics, and idempotent cancellation while leaving the full-scan probe at zero. The solution adds jqwik mixed-operation properties against a simple active-job reference model.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:15-deadline-ordered-job-collection:starter:starterAcceptanceTest
./gradlew verifyExercise15
./gradlew :exercises:15-deadline-ordered-job-collection:solution:jmhSmoke
./gradlew :exercises:15-deadline-ordered-job-collection:solution:jmh
```

## Progressive hints: observation, mechanism, design direction

1. Identify the query made by every `poll`.
2. Use an ordered heap for that query.
3. Keep a separate active-ID index.
4. Make a heap entry stale when cancellation or replacement removes its identity mapping.
5. Compare mixed operations with a simple, obviously correct reference model.

## Common wrong solutions and why they fail

- An `ArrayList` minimum scan is linear.
- Sorting the list every time repeats unnecessary work.
- A heap alone cannot make identity cancellation idempotent and efficient.
- Removing arbitrary `PriorityQueue` entries by value is linear unless additional indexing is maintained.
- Saying every `poll` is `O(log n)` ignores a cancel-heavy stale backlog.

## Production and emulator boundaries

Define durability, leader ownership, fencing, retry, job idempotency, clock source, backpressure, and quotas separately in a real scheduler. The JMH benchmark has steady poll/reschedule and cancel/schedule/poll methods at two active-job sizes; it is a local workload sample, not a service SLO.

## Specialist extension

Extend the jqwik model with concurrent-operation contracts, then compare lazy deletion with an indexed heap or a `TreeSet`-based exact-removal design. Report memory, cancel cost, and the worst single-poll latency under cancellation bursts.

## Interview follow-up questions

- Why is stale heap entry removal safe here?
- What is the amortized argument, and what does it fail to promise for one poll?
- Which invariant changes when the same ID is rescheduled?
- How would multiple schedulers avoid duplicate dispatch?

## Reflection and future-post evidence

Capture the full-scan failure, the jqwik reference-model property, JMH workload metadata, and an honest cancel-heavy trace showing why data-structure choice follows access patterns.

## Primary references

- [PriorityQueue API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)
- [Map.remove(key, value) API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html#remove(java.lang.Object,java.lang.Object))
- [jqwik user guide](https://jqwik.net/docs/current/user-guide.html)
