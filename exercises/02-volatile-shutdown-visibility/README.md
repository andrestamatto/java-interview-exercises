# Volatile Shutdown Visibility

**Category:** Java Memory Model
**Difficulty:** Advanced
**Estimated time:** 75 minutes

## Scenario, symptoms, and business impact

During a deployment, a background worker should stop after receiving a shutdown
request. The starter uses an ordinary boolean flag, so the worker is allowed to
keep reading a cached value and continue processing after the grace period.

## Learning objectives and prerequisites

Establish the happens-before edge required to publish a one-way shutdown signal.
Complete exercise 01 first; this exercise intentionally separates visibility
from atomic compound updates.

## Underlying cause

Without synchronization, a write by one thread need not become visible to a
reader in another thread. A short successful local run does not prove the Java
Memory Model contract.

## System invariants

- The signal moves only from running to stop-requested.
- Once observed as stop-requested, it is never observed as running again.
- A valid stop request is safely published to cooperating readers.

## Delivery and consistency guarantees

`volatile` write/read establishes a happens-before edge when the reader observes
that write. It provides visibility and ordering for this one field; it is not a
transaction or a compound-operation lock.

## Process-local versus distributed guarantees

The signal applies to one JVM. It does not drain HTTP requests, stop remote work,
coordinate replicas, or provide an orchestrator termination protocol.

## Functional requirements

Keep the public API unchanged. `requestStop()` must safely publish the signal;
`isStopRequested()` must read it with the matching visibility semantics.

## Non-functional requirements and resource limits

The read path must not allocate, lock, sleep, or spin on behalf of callers.

## Constraints and forbidden shortcuts

Do not use a timed test that waits until a stale read happens. Do not replace the
exercise with `synchronized`, a database, or thread interruption. Those may be
valid tools elsewhere but do not teach this publication contract.

## Architecture or sequence diagram

```text
shutdown thread: volatile write(true)  happens-before  worker: volatile read(true)
```

## Deterministic failure reproduction

The acceptance test inspects the state field's modifier. The starter compiles
and behaves locally, but intentionally lacks the required publication mechanism;
the test fails without relying on scheduler timing.

## Task and automated acceptance criteria

Make the starter acceptance test pass. The solution also compiles an optional
jcstress scenario, which records the honest race outcomes rather than claiming
that a stale read will be reproduced every run.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:02-volatile-shutdown-visibility:starter:check
./gradlew :exercises:02-volatile-shutdown-visibility:starter:starterAcceptanceTest
./gradlew :exercises:02-volatile-shutdown-visibility:solution:check
./gradlew :exercises:02-volatile-shutdown-visibility:solution:jcstress
```

Use `gradlew.bat` on Windows. The starter acceptance task is expected to fail
until you fix it. jcstress is diagnostic evidence, not an ordinary PR gate.

## Progressive hints

1. **Observation:** identify whether the defect is visibility or a lost update.
2. **Mechanism:** find the Java field modifier that creates the relevant
   happens-before edge.
3. **Design direction:** apply it only to the state whose publication is needed.

## Common wrong solutions and why they fail

- An ordinary boolean has no cross-thread visibility guarantee.
- `volatile` does not make `if (available) available--` atomic.
- Sleeping merely changes timing.
- `Thread.interrupt()` is a separate cooperative cancellation mechanism.

## Production and emulator boundaries

This proves a local memory-visibility contract. It does not prove that a worker
will reach a checkpoint promptly or that a service has drained work safely.

## Specialist extension

Add a bounded worker loop and compare `volatile` shutdown with interruption;
state which blocking operations respond to each mechanism and why both may be
needed in a real service.

## Interview follow-up questions

- What exact happens-before relation does this code rely on?
- Why is `volatile` insufficient for stock reservation?
- When should interruption be preferred?
- How would graceful shutdown work across replicas?

## Reflection and future-post evidence

Record the difference between a deterministic structural contract and a
probabilistic JMM observation, plus the limit of the process-local guarantee.

## Primary references

- [JLS 17.4: Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [Java volatile fields tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/atomic.html)
- [jcstress project](https://openjdk.org/projects/code-tools/jcstress/)
