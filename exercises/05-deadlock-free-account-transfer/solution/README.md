# Reference Solution: Deadlock-Free Account Transfer

The solution calculates a stable order from the unique account IDs before
acquiring a lock. Every transfer therefore waits for the same first account,
which removes circular wait. It still uses `try/finally` so both locks are
released after insufficient funds, interruption, or a later implementation
failure.

`lockInterruptibly` is used for recoverable local shutdown. It is not a
substitute for the lock order: interruption is recovery, while stable ordering
is prevention.

## Correctness and liveness

The service validates distinct non-null accounts and a positive amount before
locking. It derives a strict account-ID order before acquiring either lock, so a
wait-for cycle would require an ID to be both lower and higher than itself. This
assumes stable unique IDs and one live `Account` lock object per logical account
in this JVM. Both balance changes occur while both locks are held. Insufficient
funds or an uncreditable amount return `false` before mutation; a success debits
and credits the same amount. Nested `try/finally` releases acquired locks after
normal return, rejection, interruption, or unchecked failure.

## Resources and recovery

Ordering and local work are O(1), using two `ReentrantLock`s. Unrelated account
pairs can proceed, but conflicting work waits and non-fair locks offer no
fairness or tail-latency bound. Do not perform I/O or remote calls in the
critical section. `lockInterruptibly` lets a worker abandon a local wait; the
outer `finally` releases the first lock if interruption occurs while waiting for
the second. Crash, persistence failure, lost responses, and remote side effects
need durable transactions or conditional writes, idempotency, audits, and
reconciliation.

## Alternatives and operational boundary

Source-then-destination locking retains the cycle. Timeout/retry can livelock;
a global lock serializes unrelated accounts; `tryLock` lacks a retry/fairness
policy; and `identityHashCode` collisions need a tie-breaker. The proof does not
coordinate replicas, separate object graphs for one account, database locks,
payment gateways, or HTTP retries. Authenticate and authorize principal,
account scope, and amount; prevent enumeration; use idempotency and protected
audit records. Avoid account, balance, and customer data in high-cardinality
labels or raw logs. Observe lock wait/hold distributions, interruptions,
rejection outcomes, and executor saturation. Thread dumps, JFR, and
`ThreadMXBean` are incident diagnostics, not prevention.

## Evidence and primary references

The acceptance test pauses transfers after their first lock. The broken starter
lets both opposite transfers reach that point with conflicting locks; with total
ordering both require the same first lock, so both callbacks cannot be reached
before release. It is a deterministic prevention gate, not a deadlock formed
and sampled through `ThreadMXBean`. Unit tests cover normal, invalid, and
overflow-rejected transfers.

```shell
./gradlew :exercises:05-deadlock-free-account-transfer:solution:check
./gradlew verifyExercise05
```

- [ReentrantLock API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)
- [ThreadMXBean API](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/ThreadMXBean.html)
- [JDK diagnostic tools](https://docs.oracle.com/en/java/javase/21/troubleshoot/diagnostic-tools.html)
