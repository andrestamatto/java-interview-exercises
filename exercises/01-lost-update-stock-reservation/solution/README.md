# Reference solution: atomic conditional transition

The solution stores remaining stock in `AtomicInteger` and uses a compare-and-set
loop. A successful CAS is the successful reservation's linearization point. A
read that observes insufficient stock is the rejection's linearization point.

After a failed CAS, the command reloads the value and re-evaluates availability.
This matters: retrying a subtraction derived from stale state can violate the
business rule. The successful transition is atomic and `AtomicInteger` supplies
the required visibility for `remainingUnits()`.

The algorithm is lock-free, not wait-free: a failed CAS means another caller made
progress, but one caller may repeatedly lose. Work is O(1) without contention
and O(r) for r failed retries. Under extreme contention a monitor may use fewer
CPU cycles; measure that separately instead of assuming CAS is always faster.

`volatile`, separate atomic `get`/`set`, `LongAdder`, and decrement-then-rollback
do not implement the required conditional transition. A synchronized instance
method could be correct, but demonstrates a different progress mechanism.

The guarantee ends at this object in this JVM. Production replicas need a shared
authority such as a database conditional update or version check. Durable
reservations also need identity, expiry/cancellation rules, retry idempotency,
authorization, auditability, and operational signals such as conflict/retry
rates. Never record SKU or customer data in unbounded metric labels.

## Correctness, resource, and failure analysis

Construction rejects a negative initial amount and `reserve` rejects a
non-positive request before a shared-state write. A successful CAS replaces an
observed value at least as large as the request with the value minus that
request. A failed CAS and an insufficient-stock result write nothing. Thus,
inductively, remaining stock never becomes negative and successful reservations
plus the remaining amount equal the initial amount for this one object.

The uncontended operation is O(1) and allocation-free. Contention costs one
retry per conflicting successful state change and consumes CPU/cache bandwidth;
lock-free progress does not promise fairness or a latency bound. Invalid input
fails before mutation, but process loss and a lost client response have no
recovery semantics here. A real reservation needs a durable conditional write,
an idempotency key, expiry/cancellation rules, and an outbox if it emits events.

## Alternatives, boundaries, and evidence

`volatile`, separate atomic `get`/`set`, `LongAdder`, and decrement-then-rollback
cannot express this conditional linearizable transition. A monitor can be
correct locally but has a different blocking trade-off. The proof does not cover
replicas, two counters for one SKU, persistence, payment, or HTTP retries.
Authenticate and authorize inventory commands, avoid sensitive stock/customer
data in errors or high-cardinality labels, and measure bounded-cardinality
success, rejection, and contention signals.

`ReservableStockAcceptanceTest` deterministically releases two callers after
they observed the final unit and requires one reservation; the property test
checks the sequential conservation model and validation.

```shell
./gradlew :exercises:01-lost-update-stock-reservation:solution:check
./gradlew verifyExercise01
```

- [AtomicInteger API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)
- [JLS 17.4: Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [jqwik User Guide](https://jqwik.net/docs/current/user-guide.html)
