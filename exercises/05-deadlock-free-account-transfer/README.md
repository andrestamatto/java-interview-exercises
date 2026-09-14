# Deadlock-Free Account Transfer

**Category:** Concurrency, liveness, and diagnostics
**Difficulty:** Advanced
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

Two payment requests transfer money in opposite directions. Each request locks
its source account before its destination, so both threads can wait forever.
Balances stop moving, workers are retained, and queued requests accumulate.

## Learning objectives and prerequisites

Build a total lock order, distinguish safety from liveness, and understand what
JVM diagnostics reveal after a real lock cycle. Complete exercise 01 first.

## Underlying cause

The starter chooses lock order from request direction. Opposing requests form
the circular-wait condition required for deadlock.

## System invariants

- A transfer changes both balances or neither.
- A successful transfer preserves the total balance.
- Opposing transfers never form a JVM lock cycle.
- Every acquired lock is released when validation or transfer work fails.

## Delivery and consistency guarantees

The solution imposes a stable account-ID order before acquiring either lock.
`lockInterruptibly` also lets shutdown recover a blocked local worker. This is
not a durable transaction boundary.

## Process-local versus distributed guarantees

The exercise covers `ReentrantLock` instances in one JVM. It does not model
database locks, payment idempotency, account persistence, or cross-service
transactions.

## Functional requirements

Keep the public transfer API, reject invalid requests, preserve balances, and
fix the starter without weakening its acceptance test.

## Non-functional requirements and resource limits

Use a deterministic total order. Do not use retry loops, one global lock, or
timeouts as normal deadlock prevention.

## Constraints and forbidden shortcuts

Do not order locks with `identityHashCode`, because collisions require a
separate tie-breaker. Do not alter test coordination, remove locking, or make a
failed transfer partially visible.

## Architecture or sequence diagram

```text
request A -> lock lower account ID -> lock higher account ID -> transfer -> unlock
request B -> lock lower account ID -> lock higher account ID -> transfer -> unlock
```

## Deterministic failure reproduction

The acceptance probe pauses a transfer after its first lock. The starter lets
both opposing transfers reach that point while holding different accounts, which
is the exact precondition for circular wait. The test fails as soon as it
observes both conflicting first locks, then releases work for clean shutdown.
With a total account-ID order, both transfers require the same first lock, so
only one reaches the probe before release and circular wait cannot form.

This is a deterministic prevention gate. It does not form a deadlock and poll
`ThreadMXBean`; thread dumps, JFR, and `ThreadMXBean` remain manual diagnostic
extensions.

## Task and automated acceptance criteria

Fix the starter so normal tests and its acceptance task pass. The starter
acceptance task is intentionally red before the change.

## Exact build, test, run, and diagnostic commands

```shell
./gradlew :exercises:05-deadlock-free-account-transfer:starter:starterAcceptanceTest
./gradlew :exercises:05-deadlock-free-account-transfer:solution:check
./gradlew verifyExercise05
jcmd <pid> Thread.print -l
jcmd <pid> JFR.start name=deadlock settings=profile filename=deadlock.jfr
```

The JFR commands are manual diagnostic practice, not an automated proof or a
production performance claim.

## Progressive hints

1. **Observation:** list the locks acquired by each request direction.
2. **Mechanism:** eliminate circular wait with a value both requests share.
3. **Design direction:** determine the complete order before acquiring a lock.

## Common wrong solutions and why they fail

- Locking source then destination preserves the cycle.
- Retrying after a timeout hides overload and can livelock.
- A global transfer lock removes the cycle but needlessly serializes accounts.
- `tryLock` alone still needs a defined retry and fairness policy.

## Production and emulator boundaries

No database or cloud emulator is used. Database deadlocks need short,
transactional lock scopes, rollback, bounded retry, and SQL-level evidence.

## Specialist extension

Capture the starter with JFR or a thread dump, add multi-account transfer
ordering, then compare local locking with PostgreSQL pessimistic locking in
exercise 35.

## Interview follow-up questions

- Which Coffman condition does a total lock order remove?
- Why is timeout recovery not primary prevention?
- What information does a JVM thread dump provide that a metric does not?
- How would database lock ordering differ from object locking?

## Reflection and future-post evidence

Capture the forced wait-for cycle, the thread-dump evidence, and the explicit
scope of a local lock-order guarantee.

## Primary references

- [ReentrantLock API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)
- [ThreadMXBean API](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/ThreadMXBean.html)
- [JFR Runtime Guide](https://docs.oracle.com/en/java/javase/21/troubleshoot/diagnostic-tools.html)
