# Reference solution: conditional quota transition

The solution retries a compare-and-set only while a positive value remains. Each
failed CAS proves that another caller changed the state, so the loop reloads and
re-evaluates availability. A successful CAS consumes exactly one permit.

This is a lock-free process-local transition, not a fair distributed rate
limiter. A production tenant quota needs a shared authority, state expiry,
idempotency, authorization, and observability of rejections and contention.

## Correctness, resource, and failure analysis

The constructor prohibits a negative quota. Every successful CAS changes a
positive value to its non-negative predecessor and consumes exactly one permit;
this is the `true` linearization point. A failed CAS writes nothing, reloads the
changed state, and re-evaluates. Once zero is observed, `false` returns without
a write. Successful acquisitions plus available permits consequently remain the
initial quota. The uncontended call is O(1) and allocation-free. Under
contention each retry follows a conflicting state change; this finite,
decrement-only exercise is lock-free but neither wait-free nor fair.

Exhaustion is an expected result and invalid construction fails before state is
published. Crash, timeout, replenishment/window policy, and retry recovery are
absent; a real tenant quota needs a durable shared authority and idempotency.

## Alternatives, boundaries, and evidence

`get()` then `decrementAndGet()`, a volatile field, and decrement-rollback all
break the conditional invariant. A global lock remains process-local, and a
semaphore has different ownership and fairness semantics. Replicas and HTTP
retries are outside this object. Emit bounded-cardinality accepted/rejected and
contention signals, do not disclose another tenant's quota, and authenticate and
authorize tenant identity before quota evaluation.

The deterministic acceptance test releases two callers after both observed the
final permit and requires one `true`, one `false`, and zero remaining. The
property test checks the sequential reference state machine.

```shell
./gradlew :exercises:03-atomic-quota-compound-operation:solution:check
./gradlew verifyExercise03
```

- [AtomicInteger API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)
- [JLS 17: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
