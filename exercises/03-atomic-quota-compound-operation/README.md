# Atomic Quota Compound Operation

**Category:** Concurrency and atomicity
**Difficulty:** Advanced
**Estimated time:** 90 minutes

## Scenario, symptoms, and business impact

An API has one remaining quota permit. Two callers both observe it and each
continues, exceeding a customer limit despite using `AtomicInteger`.

## Learning objectives and prerequisites

Distinguish an atomic variable operation from an atomic business transition.
Complete exercises 01 and 02 first.

## Underlying cause

The starter splits observation, decision, and decrement across multiple atomic
operations. Another caller can change the value between them.

## System invariants

- Available permits never become negative.
- Successful acquisitions plus available permits equal the initial quota.
- A rejected acquisition leaves state unchanged.

## Delivery and consistency guarantees

One `RequestQuota` instance is linearizable. A successful compare-and-set is the
linearization point.

## Process-local versus distributed guarantees

This protects one JVM object, not a tenant quota across replicas or an HTTP retry.

## Functional requirements

Keep the API. Return true only when exactly one permit is consumed.

## Non-functional requirements and resource limits

Do not use sleeps, global locks, or unbounded retries.

## Constraints and forbidden shortcuts

Do not weaken acceptance tests or clamp a negative value after the fact.

## Architecture or sequence diagram

```text
read available -> conditional decrement if unchanged -> retry with new state
```

## Deterministic failure reproduction

Two callers are held after observing the final permit, then released together.
The starter consumes it twice; the solution permits one success.

## Task and automated acceptance criteria

Fix the starter so its normal tests, property model, and explicit acceptance task pass.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:03-atomic-quota-compound-operation:starter:starterAcceptanceTest
./gradlew :exercises:03-atomic-quota-compound-operation:solution:check
./gradlew verifyExercise03
```

The starter acceptance task is intentionally red before the change. No benchmark
is claimed by this correctness exercise.

## Progressive hints

1. **Observation:** list every operation in the business transition.
2. **Mechanism:** find a conditional atomic update.
3. **Design direction:** reload and re-evaluate after a failed update.

## Common wrong solutions and why they fail

- `get()` followed by `decrementAndGet()` is still a race.
- `volatile` gives visibility, not conditional atomicity.
- Decrement then rollback exposes invalid state.

## Production and emulator boundaries

There is no persistence, retry idempotency, rate-window policy, or multi-instance coordination.

## Specialist extension

Compare the CAS loop with a semaphore and explain their fairness/progress trade-offs.

## Interview follow-up questions

- Where is the linearization point?
- Why is this lock-free but not wait-free?
- How would Redis or DynamoDB implement the same conditional transition?

## Reflection and future-post evidence

Capture the forced last-permit race and why an atomic holder was insufficient.

## Primary references

- [AtomicInteger API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)
- [JLS 17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
