# Index Membership Lookups

**Category:** Algorithmic performance
**Difficulty:** Advanced
**Estimated time:** 75 minutes

## Scenario, symptoms, and business impact

Authorization reconciliation scans every grant for every requested product.
Large customer imports consume CPU even when almost no products match.

## Learning objectives and prerequisites

Replace repeated membership scans with an index while preserving request order
and duplicates. Complete exercise 04 first.

## Underlying cause

The starter performs a nested equality scan: `requests × grants` in the
no-match case.

## System invariants

- Output retains the order and duplicates of requests.
- Only granted requests are emitted.
- Input lists are not mutated.

## Delivery and consistency guarantees

The operation is deterministic in one process. `HashSet` has expected average
constant-time membership, not a worst-case latency promise.

## Process-local versus distributed guarantees

This is not authorization enforcement, cache invalidation, or tenant isolation.

## Functional requirements

Keep both overloads and make normal/acceptance tests pass.

## Non-functional requirements and resource limits

Build one index per call; do not use elapsed time as a quality gate.

## Constraints and forbidden shortcuts

Do not sort requests, remove duplicates, change inputs, or weaken the counter.

## Architecture or sequence diagram

```text
grants -> HashSet index -> ordered request scan -> allowed requests
```

## Deterministic failure reproduction

Twenty absent requests and twenty absent grants require 400 equality probes in
the starter. The acceptance test observes the counter, not wall-clock time.

## Task and automated acceptance criteria

Preserve output semantics and reduce repeated membership comparisons.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:11-index-membership-lookups:starter:starterAcceptanceTest
./gradlew verifyExercise11
```

## Progressive hints

1. Count the innermost operation.
2. Identify membership as the repeated question.
3. Build an index before iterating requests.

## Common wrong solutions and why they fail

- Sorting changes order/duplicate semantics.
- A list copied once is still linear for `contains`.
- Time assertions fail across machines.

## Production and emulator boundaries

Hash distribution, memory pressure, and adversarial keys need production data.

## Specialist extension

Use JMH with realistic match ratios and compare allocation from index creation.

## Interview follow-up questions

- Why is expected hash complexity not a hard latency guarantee?
- When would a sorted representation be preferable?

## Reflection and future-post evidence

Capture the 400-probe reproduction and the preserved output semantics.

## Primary references

- [HashSet API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html)
