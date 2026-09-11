# Lost Update in Stock Reservation

**Category:** Concurrency and the Java Memory Model
**Difficulty:** Advanced
**Estimated time:** 75 minutes

## Scenario, symptoms, and business impact

A flash-sale service keeps a process-local stock counter. Two request threads can
both observe the last available unit and both report a successful reservation.
The visible counter may look plausible, but the company has promised one unit
twice, causing refunds, fulfillment failures, and support work.

## Learning objectives and prerequisites

Identify a lost update, define a linearization point, and preserve a business
invariant under contention. Basic Java threads, executors, and tests are enough;
Spring is deliberately absent from this low-level JVM lesson.

## Underlying cause

The starter performs read, decision, and write as separate actions. Individual
reads and writes do not make the complete check-then-update transition atomic.

## System invariants

- Remaining stock is never negative.
- Reserved units plus remaining units equal the initial stock.
- A rejected reservation does not change stock.
- Each call returns one unambiguous outcome.

## Delivery and consistency guarantees

The target is a linearizable transition for one in-memory `ReservableStock`
instance. A successful call appears to take effect once between invocation and
response.

## Process-local versus distributed guarantees

This does not coordinate multiple objects, JVMs, database rows, retried requests,
or remote payments. Those require different protocols covered later.

## Functional requirements

- Keep the public API unchanged.
- Reject non-positive quantities.
- Reject a request larger than stock at its linearization point.
- Preserve all invariants for sequential and concurrent callers.

## Non-functional requirements and resource limits

- Do not serialize unrelated stock instances through global state.
- Do not busy-wait or create a thread per reservation.
- Keep the transition small enough to explain in a senior interview.

## Constraints and forbidden shortcuts

Do not alter or disable acceptance tests, add sleeps, access solution output, or
return success without consuming stock. Invoke the package-private test probe
once after the first observation, even when your algorithm retries.

## Deterministic failure reproduction

Two callers reserve the final unit. A latch holds both after their first read and
then releases them together. The starter reports two successes; exactly one is
valid. This uses no scheduler luck or arbitrary delay.

## Task and automated acceptance criteria

Change only the starter implementation so shared tests pass. Completion requires
validation, the sequential-model property, the forced lost-update test,
format/static checks, and isolation from solution code.

## Exact commands

```shell
./gradlew :exercises:01-lost-update-stock-reservation:starter:check
./gradlew :exercises:01-lost-update-stock-reservation:starter:starterAcceptanceTest
./gradlew :exercises:01-lost-update-stock-reservation:solution:check
./gradlew verifyExercise01 verifyStarterIsolation
```

Use `gradlew.bat` on Windows. Before your change, `starterAcceptanceTest` is
expected to fail. There is no runner, Docker test, or JMH benchmark: this
exercise makes a correctness claim, not a throughput claim.

## Progressive hints

1. **Observation:** name the actions that must behave as one transition.
2. **Mechanism:** find an API that replaces a value only if it still equals the
   value previously observed.
3. **Design direction:** after a failed conditional update, reload state and
   check the business rule again.

## Common wrong solutions and why they fail

- `volatile int` provides visibility but not compound-operation atomicity.
- Separate `AtomicInteger.get()` and `set()` calls still lose updates.
- Decrement-then-rollback exposes invalid state and introduces another race.
- `LongAdder` is not a linearizable conditional counter.
- Probabilistic stress loops do not deterministically reproduce the defect.

## Production and emulator boundaries

Tests prove the named process-local interleaving and invariants. They do not
prove fairness, production capacity, crash recovery, or distributed inventory.
No cloud emulator is involved.

## Specialist extension

Compare a monitor and a CAS loop under controlled contention. Use JMH for any
performance claim and report distributions and environment rather than inferring
production capacity from a microbenchmark.

## Interview follow-up questions

- What is your linearization point?
- Is the solution blocking, lock-free, or wait-free?
- When can optimistic retries perform worse than locking?
- How would PostgreSQL or DynamoDB protect this invariant across JVMs?
- Where would HTTP idempotency fit?

## Reflection and future-post evidence

Record the forced interleaving, violated invariant, chosen linearization point,
green acceptance output, and precise limits. Do not claim that an atomic counter
solves distributed inventory.

## Primary references

- [JLS 17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [AtomicInteger API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)
- [JUnit 5 user guide](https://docs.junit.org/5.14.1/user-guide/)
- [jqwik user guide](https://jqwik.net/docs/current/user-guide.html)
